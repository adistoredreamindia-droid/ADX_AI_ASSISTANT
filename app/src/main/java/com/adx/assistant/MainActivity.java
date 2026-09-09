package com.adx.assistant;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.os.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    static final String PREF="adx_private", KEY="gemini_api_key";
    SharedPreferences pref;
    ExecutorService pool=Executors.newSingleThreadExecutor();
    Handler ui=new Handler(Looper.getMainLooper());
    TextToSpeech tts; SpeechRecognizer recognizer; ADXView v;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(6,15,28));
        getWindow().setNavigationBarColor(Color.rgb(12,22,39));
        pref=getSharedPreferences(PREF,0);
        tts=new TextToSpeech(this,s->{if(s==TextToSpeech.SUCCESS)tts.setLanguage(Locale.ENGLISH);});
        v=new ADXView(this); setContentView(v);
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA},10);
    }
    boolean keySet(){return !pref.getString(KEY,"").trim().isEmpty();}

    void keyDialog(){
        final EditText e=new EditText(this); e.setSingleLine(true); e.setHint("AIza..."); e.setText(pref.getString(KEY,""));
        LinearLayout box=new LinearLayout(this); box.setPadding(35,5,35,0); box.addView(e,new LinearLayout.LayoutParams(-1,-2));
        new AlertDialog.Builder(this).setTitle("Gemini API key")
            .setMessage("Paste your Gemini API key. ADX keeps it on this phone and connects directly to Google Gemini.")
            .setView(box).setNegativeButton("Remove",(d,w)->{pref.edit().remove(KEY).apply();v.invalidate();})
            .setPositiveButton("Save & test",(d,w)->{String k=e.getText().toString().trim();if(k.isEmpty()){toast("API key is empty");return;}pref.edit().putString(KEY,k).apply();v.invalidate();gemini(k,"Reply exactly: ADX connected",false);}).show();
    }

    void chatInput(){
        if(!keySet()){toast("First add Gemini API key in Settings");v.screen=3;v.invalidate();return;}
        final EditText e=new EditText(this);e.setHint("Ask ADX anything…");e.setMinLines(2);
        new AlertDialog.Builder(this).setTitle("Ask ADX").setView(e).setNegativeButton("Cancel",null)
            .setPositiveButton("Send",(d,w)->{String q=e.getText().toString().trim();if(!q.isEmpty()){v.user=q;v.answer="Thinking…";v.screen=2;v.invalidate();gemini(pref.getString(KEY,""),q,true);}}).show();
    }

    void voice(){
        if(!keySet()){toast("First add Gemini API key in Settings");v.screen=3;v.invalidate();return;}
        if(!SpeechRecognizer.isRecognitionAvailable(this)){toast("Voice recognition is not available");return;}
        if(recognizer!=null)recognizer.destroy();
        recognizer=SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener(){
            public void onReadyForSpeech(Bundle b){toast("Listening…");} public void onBeginningOfSpeech(){} public void onRmsChanged(float x){}
            public void onBufferReceived(byte[] b){} public void onEndOfSpeech(){} public void onError(int x){toast("Voice error. Try again");}
            public void onResults(Bundle b){ArrayList<String>a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(a!=null&&!a.isEmpty()){v.user=a.get(0);v.answer="Thinking…";v.screen=2;v.invalidate();gemini(pref.getString(KEY,""),v.user,true);}}
            public void onPartialResults(Bundle b){} public void onEvent(int x,Bundle b){}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"hi-IN");recognizer.startListening(i);
    }

    String normalizeModel(String n){
        if(n==null)return ""; n=n.trim();
        while(n.startsWith("models/")) n=n.substring(7);
        int slash=n.indexOf('/'); if(slash>=0)n=n.substring(slash+1);
        return n;
    }

    ArrayList<String> discoverModels(String k){
        ArrayList<String> out=new ArrayList<>();
        String[] versions={"v1beta","v1"};
        for(String ver:versions){
            try{
                URL u=new URL("https://generativelanguage.googleapis.com/"+ver+"/models");
                HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setRequestMethod("GET");c.setConnectTimeout(12000);c.setReadTimeout(18000);c.setRequestProperty("x-goog-api-key",k);
                int code=c.getResponseCode();String json=read(code>=200&&code<300?c.getInputStream():c.getErrorStream());
                if(code<200||code>=300)continue;
                JSONArray a=new JSONObject(json).optJSONArray("models");if(a==null)continue;
                for(int i=0;i<a.length();i++){JSONObject m=a.getJSONObject(i);JSONArray methods=m.optJSONArray("supportedGenerationMethods");boolean gen=false;if(methods!=null)for(int j=0;j<methods.length();j++)if("generateContent".equalsIgnoreCase(methods.optString(j)))gen=true;if(gen){String n=normalizeModel(m.optString("name"));if(!n.isEmpty()&&!out.contains(n))out.add(n);}}
            }catch(Exception ignored){}
        }
        String[] preferred={"gemini-2.5-flash","gemini-2.5-flash-lite","gemini-2.0-flash","gemini-2.0-flash-lite","gemini-1.5-flash"};
        ArrayList<String> ordered=new ArrayList<>();for(String p:preferred)if(out.contains(p))ordered.add(p);for(String n:out)if(!ordered.contains(n))ordered.add(n);return ordered;
    }

    String postGemini(String k,String q,String ver,String model)throws Exception{
        model=normalizeModel(model);
        URL u=new URL("https://generativelanguage.googleapis.com/"+ver+"/models/"+model+":generateContent");
        HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=UTF-8");c.setRequestProperty("x-goog-api-key",k);
        JSONObject body=new JSONObject().put("contents",new JSONArray().put(new JSONObject().put("role","user").put("parts",new JSONArray().put(new JSONObject().put("text",q)))));
        try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes(StandardCharsets.UTF_8));}
        int code=c.getResponseCode();String json=read(code>=200&&code<300?c.getInputStream():c.getErrorStream());
        if(code<200||code>=300)throw new ApiException(code,json);
        JSONObject root=new JSONObject(json);JSONArray cs=root.optJSONArray("candidates");if(cs==null||cs.length()==0)throw new Exception("No Gemini response");
        JSONObject content=cs.getJSONObject(0).optJSONObject("content");if(content==null)throw new Exception("Gemini returned no content");JSONArray parts=content.optJSONArray("parts");if(parts==null||parts.length()==0)throw new Exception("Gemini returned no text");
        String text=parts.getJSONObject(0).optString("text","");if(text.trim().isEmpty())throw new Exception("Gemini returned empty text");return text;
    }

    static class ApiException extends Exception{int code;ApiException(int c,String b){super("HTTP "+c+": "+b);code=c;}}

    void gemini(String k,String q,boolean show){
        pool.execute(()->{
            try{
                final String apiKey=k.trim();
                ArrayList<String> models=discoverModels(apiKey);
                if(models.isEmpty())models.addAll(Arrays.asList("gemini-2.5-flash","gemini-2.0-flash","gemini-2.5-flash-lite","gemini-1.5-flash"));
                Exception last=null; String[] versions={"v1beta","v1"};
                for(String ver:versions){
                    for(String model:models){
                        try{
                            String result=postGemini(apiKey,q,ver,model);
                            final String success=result; final String successModel=model;
                            ui.post(()->{if(show){v.answer=success;v.invalidate();speak(success);}else toast("ADX connected — "+successModel);});
                            return;
                        }catch(ApiException ex){last=ex;if(ex.code==400||ex.code==401||ex.code==403)break;}
                        catch(Exception ex){last=ex;}
                    }
                    if(last instanceof ApiException && (((ApiException)last).code==400||((ApiException)last).code==401||((ApiException)last).code==403))break;
                }
                String msg=last==null?"No Gemini model available":last.getMessage();
                final String result="ERROR: "+(msg==null?"Gemini request failed":msg);
                ui.post(()->{toast(result.substring(7));if(show){v.answer="Gemini connection failed. Open Settings → Gemini API Key and test the key again.";v.invalidate();}});
            }catch(Exception ex){final String msg=ex.getMessage()==null?"Gemini request failed":ex.getMessage();ui.post(()->toast(msg));}
        });
    }

    String read(InputStream in)throws Exception{if(in==null)return "";BufferedReader b=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();String x;while((x=b.readLine())!=null)s.append(x);b.close();return s.toString();}
    void speak(String s){if(tts!=null){tts.setLanguage(Locale.ENGLISH);tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"ADX");}}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override protected void onDestroy(){if(recognizer!=null)recognizer.destroy();if(tts!=null)tts.shutdown();pool.shutdownNow();super.onDestroy();}

    class ADXView extends View{
        Paint p=new Paint(3);RectF r=new RectF();int screen=0;boolean drawer=false;String user="",answer="";
        int blue=Color.rgb(64,165,226),white=Color.rgb(245,247,252),muted=Color.rgb(150,166,193),panel=Color.rgb(27,37,58),dark=Color.rgb(18,31,51);
        ADXView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        void t(Canvas c,String s,float x,float y,float z,int col,boolean b){p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(z);p.setTypeface(Typeface.create(Typeface.DEFAULT,b?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x,y,p);}
        void rr(Canvas c,float l,float top,float right,float bot,int col,float rad){p.setColor(col);p.setStyle(Paint.Style.FILL);p.setShadowLayer(9,0,4,0x44000000);r.set(l,top,right,bot);c.drawRoundRect(r,rad,rad,p);p.clearShadowLayer();}
        void header(Canvas c,String title){t(c,screen==0?"☰":"‹",32,70,44,white,false);p.setTextSize(29);t(c,title,(getWidth()-p.measureText(title))/2,66,29,white,true);t(c,"•",getWidth()-90,57,25,blue,true);t(c,"ADX",getWidth()-69,61,14,white,true);}
        @Override protected void onDraw(Canvas c){c.drawColor(Color.rgb(7,16,29));if(screen==0)home(c);else if(screen==1)memory(c);else if(screen==2)chat(c);else settings(c);if(drawer)drawer(c);}
        void home(Canvas c){header(c,"Today");rr(c,22,88,getWidth()-22,158,dark,23);t(c,keySet()?"GEMINI CONNECTED  •  PERSONAL ADX":"SETUP REQUIRED  •  PERSONAL ADX",40,116,13,blue,true);t(c,keySet()?"Voice, memory and Gemini chat are ready":"Add your Gemini API key in Settings to start",40,143,14,muted,false);t(c,"Good afternoon,",36,208,25,muted,true);t(c,"Aditya",36,255,42,white,true);t(c,"ADX is ready to work.",36,288,18,muted,false);rr(c,34,322,getWidth()-34,490,Color.rgb(10,35,55),33);t(c,"ADX",getWidth()/2-30,388,27,blue,true);t(c,keySet()?"Gemini AI Assistant":"Personal AI Assistant",getWidth()/2-92,420,17,muted,false);t(c,keySet()?"Ask, speak, and get answers":"Add API key to activate",getWidth()/2-100,452,15,white,false);button(c,24,515,190,578,"Focus");button(c,205,515,371,578,"Tasks");button(c,386,515,getWidth()-24,578,"News");info(c,24,598,190,690,"Weather","—","No data");info(c,205,598,371,690,"Today","9","Wed, Sep");info(c,386,598,getWidth()-24,690,"Mood","Happy","Upbeat");rr(c,24,getHeight()-145,getWidth()-24,getHeight()-84,dark,28);t(c,"⌕",44,getHeight()-107,24,muted,false);t(c,"Ask ADX anything…",80,getHeight()-109,17,muted,false);t(c,"➤",getWidth()-64,getHeight()-106,24,blue,true);bottom(c,0);}
        void button(Canvas c,float l,float top,float right,float bot,String s){rr(c,l,top,right,bot,Color.rgb(20,31,53),23);t(c,s,l+35,top+40,16,white,true);}
        void info(Canvas c,float l,float top,float right,float bot,String a,String b,String d){rr(c,l,top,right,bot,Color.rgb(27,39,61),22);t(c,a,l+14,top+28,13,muted,false);t(c,b,l+14,top+62,23,white,true);t(c,d,l+14,top+82,12,muted,false);}
        void memory(Canvas c){header(c,"Memories");t(c,"Trained tasks",34,132,28,white,true);card(c,34,158,"gemini se poochho");card(c,34,245,"chatgpt se poochho");card(c,34,332,"gemini image");rr(c,34,430,getWidth()-34,515,panel,27);t(c,"☁",55,480,25,muted,true);t(c,"Backup & restore",98,471,20,white,true);t(c,"Move memories and chats to another phone",98,496,13,muted,false);bottom(c,1);}
        void card(Canvas c,float l,float top,String s){rr(c,l,top,getWidth()-34,top+72,panel,25);t(c,s,l+26,top+36,20,white,true);t(c,"AI prompt  •  built-in",l+26,top+59,13,blue,false);t(c,"▶ Play",getWidth()-120,top+44,15,blue,true);}
        void chat(Canvas c){header(c,"Chat");if(user.isEmpty()){t(c,"ADX",30,130,20,blue,true);t(c,"Hello! I am ready.",30,164,22,white,true);t(c,keySet()?"Ask me anything using Gemini.":"Add your Gemini API key in Settings.",30,192,15,muted,false);}else{rr(c,34,118,getWidth()-34,192,Color.rgb(20,35,56),22);t(c,"You",54,145,13,blue,true);wrap(c,user,54,168,15,white);if(!answer.isEmpty()){rr(c,34,220,getWidth()-34,430,panel,22);t(c,"ADX",54,247,13,blue,true);wrap(c,answer,54,272,15,white);}}rr(c,28,getHeight()-150,getWidth()-28,getHeight()-88,dark,30);t(c,"Type a message…",52,getHeight()-112,17,muted,false);t(c,"➤",getWidth()-70,getHeight()-109,24,blue,true);bottom(c,2);}
        void wrap(Canvas c,String s,float x,float y,float z,int col){p.setTextSize(z);String line="";float yy=y;for(String w:s.split(" ")){String n=line.isEmpty()?w:line+" "+w;if(p.measureText(n)>getWidth()-x-45){t(c,line,x,yy,z,col,false);line=w;yy+=z+7;if(yy>getHeight()-220)break;}else line=n;}if(!line.isEmpty())t(c,line,x,yy,z,col,false);}
        void settings(Canvas c){header(c,"Settings");t(c,"PERSONAL",45,120,16,muted,true);setting(c,34,140,"Personal","Your name, language and AI provider","●");t(c,"ASSISTANT",45,238,16,muted,true);setting(c,34,258,"ADX","Persona, voice and assistant behavior","◉");setting(c,34,336,"Gemini API Key",keySet()?"Connected • tap to change key":"Not connected • tap to add key","ϟ");setting(c,34,414,"Voice","Hindi / English voice input and spoken answers","♬");t(c,"WORK & TOOLS",45,518,16,muted,true);setting(c,34,538,"Documents","PDF, Word, Excel and file tools","▤");setting(c,34,616,"Website / Coding","Build and explain websites and code","<>");setting(c,34,694,"Study / Whiteboard","Learn, explain and draw","✎");}
        void setting(Canvas c,float l,float top,String a,String b,String icon){rr(c,l,top,getWidth()-34,top+66,panel,24);t(c,icon,l+20,top+39,21,blue,true);t(c,a,l+67,top+29,19,white,true);t(c,b,l+67,top+51,12,muted,false);t(c,"›",getWidth()-61,top+41,28,muted,false);}
        void bottom(Canvas c,int sel){int y=getHeight()-72;p.setColor(Color.rgb(35,50,75));c.drawRect(0,y-2,getWidth(),y,p);nav(c,55,y,"⌂","Home",sel==0);nav(c,165,y,"◎","Scan",false);nav(c,getWidth()/2,y,"●","Voice",false);nav(c,getWidth()-165,y,"◉","Memory",sel==1);nav(c,getWidth()-55,y,"▰","Chat",sel==2);}
        void nav(Canvas c,float x,float y,String i,String s,boolean on){int col=on?blue:muted;t(c,i,x-10,y-25,23,col,true);p.setTextSize(13);t(c,s,x-p.measureText(s)/2,y-2,13,col,on);}
        void drawer(Canvas c){p.setColor(0x99000000);c.drawRect(0,0,getWidth(),getHeight(),p);float right=Math.min(getWidth()-45,690);rr(c,0,0,right,getHeight(),Color.rgb(19,29,49),34);t(c,"ADX",45,70,33,white,true);t(c,"Personal AI Assistant",45,98,15,muted,false);item(c,140,"⌂","ADX Home");item(c,207,"◉","Memories");item(c,274,"▰","Chat");t(c,"PRODUCTIVITY",52,345,15,muted,true);item(c,375,"⌁","Markets");item(c,442,"▤","Documents");item(c,509,"<>","Website / Coding");item(c,576,"✎","Study / Whiteboard");t(c,"SYSTEM",52,640,15,muted,true);item(c,675,"⚙","Settings");item(c,742,"✓","ADX Rules");}
        void item(Canvas c,int y,String i,String s){rr(c,25,y-40,Math.min(getWidth()-75,650),y+24,Color.rgb(31,41,62),31);t(c,i,50,y,21,muted,true);t(c,s,102,y,19,white,false);}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();if(drawer){if(x<Math.min(getWidth()-45,690)){if(y>105&&y<175){screen=0;drawer=false;}else if(y>=175&&y<240){screen=1;drawer=false;}else if(y>=240&&y<315){screen=2;drawer=false;}else if(y>=650&&y<720){screen=3;drawer=false;}invalidate();}else{drawer=false;invalidate();}return true;}if(y<105&&x<120){if(screen==0)drawer=true;else screen=0;invalidate();return true;}if(screen==3&&y>=320&&y<=420){keyDialog();return true;}if(screen==0&&y>getHeight()-175&&y<getHeight()-70){chatInput();return true;}if(screen==0&&y>310&&y<500){voice();return true;}if(screen==2&&y>getHeight()-165){chatInput();return true;}if(y>getHeight()-85){if(x<110)screen=0;else if(x>getWidth()-110)screen=2;else if(x>getWidth()/2+70)screen=1;else if(x>getWidth()/2-70&&x<getWidth()/2+70)voice();invalidate();return true;}return true;}
    }
}

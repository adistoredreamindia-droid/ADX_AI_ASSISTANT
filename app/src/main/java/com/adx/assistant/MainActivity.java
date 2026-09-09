package com.adx.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.Toast;

public class MainActivity extends Activity {
    ADXView view;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(7, 16, 29));
        getWindow().setNavigationBarColor(Color.rgb(12, 22, 39));
        view = new ADXView();
        setContentView(view);
        if (android.os.Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 10);
        }
    }

    class ADXView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int screen = 0; // 0 home, 1 memories, 2 chat, 3 settings, 4 rules
        boolean drawer = false;
        int blue = Color.rgb(67, 165, 226);
        int text = Color.rgb(245, 247, 252);
        int muted = Color.rgb(150, 166, 193);
        int panel = Color.rgb(27, 37, 58);
        int darkPanel = Color.rgb(18, 31, 51);
        RectF r = new RectF();

        ADXView(){ super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }

        void txt(Canvas c, String s, float x, float y, float size, int col, boolean bold){
            p.setStyle(Paint.Style.FILL); p.setColor(col); p.setTextSize(size);
            p.setTypeface(Typeface.create(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL));
            c.drawText(s, x, y, p);
        }
        void round(Canvas c,float l,float t,float rr,float bb,int col,float rad){
            p.setStyle(Paint.Style.FILL); p.setColor(col); p.setShadowLayer(10,0,5,0x55000000);
            r.set(l,t,rr,bb); c.drawRoundRect(r,rad,rad,p); p.clearShadowLayer();
        }
        void strokeRound(Canvas c,float l,float t,float rr,float bb,int col,float rad){
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(col); r.set(l,t,rr,bb); c.drawRoundRect(r,rad,rad,p); p.setStyle(Paint.Style.FILL);
        }
        void line(Canvas c,float x1,float y1,float x2,float y2,int col){p.setColor(col);p.setStrokeWidth(1.5f);c.drawLine(x1,y1,x2,y2,p);}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            c.drawColor(Color.rgb(7,16,29));
            if(screen==0) home(c); else if(screen==1) memories(c); else if(screen==2) chat(c); else if(screen==3) settings(c); else rules(c);
            if(drawer) drawDrawer(c);
        }

        void top(Canvas c,String title,boolean back){
            txt(c, back ? "‹" : "☰", 38, 70, 48, text, false);
            float w=p.measureText(title); txt(c,title,(getWidth()-w)/2,66,30,text,true);
            txt(c,"•",getWidth()-88,57,28,blue,true);
            txt(c,"ADX",getWidth()-69,61,15,text,true);
        }

        void home(Canvas c){
            top(c,"Today",false);
            round(c,28,92,getWidth()-28,178,darkPanel,26);
            txt(c,"FREE MODE  •  PERSONAL ADX",52,124,14,blue,true);
            txt(c,"Voice, memory, tools and phone actions",52,153,16,muted,false);
            txt(c,"Good afternoon,",45,226,27,muted,true);
            txt(c,"Aditya",45,278,46,text,true);
            txt(c,"ADX is ready to work.",45,314,20,muted,false);

            round(c,45,350,getWidth()-45,535,Color.rgb(10,35,55),35);
            txt(c,"ADX",getWidth()/2-34,425,28,blue,true);
            txt(c,"Personal AI",getWidth()/2-62,455,18,muted,false);
            txt(c,"Ask, speak, or give me a task",getWidth()/2-118,492,16,text,false);

            cardButton(c,30,565,250,635,"Focus","◎");
            cardButton(c,270,565,490,635,"Tasks","✓");
            cardButton(c,510,565,getWidth()-30,635,"News","▤");
            smallInfo(c,30,655,250,755,"Weather","—","No data");
            smallInfo(c,270,655,490,755,"Today","9","Wed, Sep");
            smallInfo(c,510,655,getWidth()-30,755,"Mood","Happy","Upbeat");
            round(c,30,getHeight()-160,getWidth()-30,getHeight()-92,darkPanel,30);
            txt(c,"⌕",52,getHeight()-117,27,muted,false);
            txt(c,"Ask ADX anything…",92,getHeight()-119,19,muted,false);
            txt(c,"➤",getWidth()-72,getHeight()-116,26,muted,false);
            bottom(c,0);
        }

        void cardButton(Canvas c,float l,float t,float rr,float bb,String label,String icon){
            round(c,l,t,rr,bb,Color.rgb(20,31,53),26); txt(c,icon,l+18,t+44,22,muted,true); txt(c,label,l+55,t+45,18,text,true);
        }
        void smallInfo(Canvas c,float l,float t,float rr,float bb,String a,String b,String d){
            round(c,l,t,rr,bb,Color.rgb(27,39,61),25); txt(c,a,l+18,t+30,15,muted,false); txt(c,b,l+18,t+68,26,text,true); txt(c,d,l+18,t+90,14,muted,false);
        }

        void memories(Canvas c){
            top(c,"Memories",false);
            txt(c,"Trained tasks",38,138,29,text,true);
            memoryCard(c,38,165,"gemini se poochho","8 steps  •  sawaal  •  built-in");
            memoryCard(c,38,255,"chatgpt se poochho","8 steps  •  sawaal  •  built-in");
            memoryCard(c,38,345,"gemini image","17 steps  •  image prompt  •  built-in");
            memoryCard(c,38,435,"chatgpt image","14 steps  •  image prompt  •  built-in");
            round(c,38,535,getWidth()-38,625,panel,28); txt(c,"☁",62,584,27,muted,true); txt(c,"Backup & restore",110,575,21,text,true); txt(c,"Move memories and chats to another phone",110,601,15,muted,false); txt(c,"›",getWidth()-62,588,34,muted,false);
            bottom(c,1);
        }
        void memoryCard(Canvas c,float l,float t,String a,String b){
            round(c,l,t,getWidth()-38,t+76,panel,26); txt(c,a,l+30,t+38,22,text,true); txt(c,b,l+30,t+62,14,blue,false); txt(c,"▶ Play",getWidth()-142,t+46,16,blue,true);
        }

        void chat(Canvas c){
            top(c,"Chat",false);
            txt(c,"ADX",32,130,20,blue,true); txt(c,"Hello! I am ready.",32,162,22,text,true);
            txt(c,"Ask questions, plan tasks, or control your phone.",32,190,15,muted,false);
            round(c,28,235,getWidth()-28,320,Color.rgb(21,34,54),24); txt(c,"You can connect an AI provider in Settings.",48,270,16,muted,false); txt(c,"Your API key stays on your device.",48,296,14,muted,false);
            round(c,28,getHeight()-150,getWidth()-28,getHeight()-88,darkPanel,30); txt(c,"Type a message…",55,getHeight()-112,18,muted,false); txt(c,"➤",getWidth()-70,getHeight()-109,25,blue,true);
            bottom(c,2);
        }

        void settings(Canvas c){
            top(c,"Settings",true);
            txt(c,"PERSONAL",48,120,16,muted,true);
            setting(c,38,140,"Personal","Your name, language and AI provider","●");
            txt(c,"ASSISTANT",48,250,16,muted,true);
            setting(c,38,270,"ADX","Persona, voice and assistant behavior","◉");
            setting(c,38,350,"Skills","Installed tools and routines","ϟ");
            setting(c,38,430,"Sub-agents","Coding and background agents","●");
            txt(c,"WORK & MESSAGES",48,530,16,muted,true);
            setting(c,38,550,"Email","Connect an email account","✉");
            setting(c,38,630,"WhatsApp & reports","Message workflows and report formats","●");
            setting(c,38,710,"Social media","Captions and scheduled posts","↗");
        }
        void setting(Canvas c,float l,float t,String a,String b,String icon){
            round(c,l,t,getWidth()-38,t+68,panel,25); txt(c,icon,l+22,t+40,22,blue,true); txt(c,a,l+70,t+30,20,text,true); txt(c,b,l+70,t+53,13,muted,false); txt(c,"›",getWidth()-65,t+42,30,muted,false);
        }

        void rules(Canvas c){
            top(c,"ADX Rules",true);
            txt(c,"Rules you set for ADX",38,130,26,text,true);
            txt(c,"Tell ADX how you want it to behave. For example:",38,166,16,muted,false);
            txt(c,"keep answers short • call me sir • ask before sending",38,192,15,muted,false);
            txt(c,"messages",38,215,15,muted,false);
            round(c,38,245,getWidth()-38,305,blue,30); txt(c,"+  Add rule",getWidth()/2-65,284,20,Color.WHITE,true);
            txt(c,"No rules yet — ADX is using its defaults.",38,365,17,muted,false);
            txt(c,"Rules never override permissions or safety checks.",38,398,15,muted,false);
        }

        void bottom(Canvas c,int selected){
            int y=getHeight()-72; line(c,0,y-2,getWidth(),y-2,Color.rgb(35,50,75));
            nav(c,55,y,"⌂","Home",selected==0); nav(c,165,y,"◎","Scan",false); nav(c,getWidth()/2,y,"●","Voice",false); nav(c,getWidth()-165,y,"◉","Memory",selected==1); nav(c,getWidth()-55,y,"▰","Chat",selected==2);
        }
        void nav(Canvas c,float x,float y,String icon,String label,boolean on){
            int col=on?blue:muted; float iw=p.measureText(icon); txt(c,icon,x-10,y-25,24,col,true); txt(c,label,x-p.measureText(label)/2,y-2,13,col,on);
        }

        void drawDrawer(Canvas c){
            p.setColor(0x99000000); c.drawRect(0,0,getWidth(),getHeight(),p);
            round(c,0,0,Math.min(getWidth()-70,690),getHeight(),Color.rgb(19,29,49),34);
            txt(c,"ADX",48,75,34,text,true); txt(c,"Personal AI Assistant",48,103,16,muted,false);
            drawerItem(c,140,"⌂","ADX Home",0);
            drawerItem(c,207,"◉","Memories",1);
            drawerItem(c,274,"▰","Chat",2);
            txt(c,"PRODUCTIVITY",55,345,15,muted,true);
            drawerItem(c,375,"⌁","Markets",-1); drawerItem(c,442,"▤","Documents",-1); drawerItem(c,509,"<> ","Website / Coding",-1); drawerItem(c,576,"✎","Study / Whiteboard",-1);
            txt(c,"SYSTEM",55,640,15,muted,true);
            drawerItem(c,675,"⚙","Settings",3); drawerItem(c,742,"✓","ADX Rules",4);
            txt(c,"OTHER",55,805,15,muted,true);
            drawerItem(c,840,"▣","Privacy",-1); drawerItem(c,907,"i","About",-1);
            txt(c,"ADX  •  Independent personal assistant",45,getHeight()-28,13,muted,false);
        }
        void drawerItem(Canvas c,int y,String icon,String label,int target){
            round(c,28,y-40,Math.min(getWidth()-95,650),y+25,Color.rgb(31,41,62),31); txt(c,icon,52,y,22,muted,true); txt(c,label,105,y,20,text,false);
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;
            float x=e.getX(), y=e.getY();
            if(drawer){
                if(x<690){
                    if(y>105&&y<240){screen=0;drawer=false;}
                    else if(y>=240&&y<320){screen=2;drawer=false;}
                    else if(y>=650&&y<730){screen=3;drawer=false;}
                    else if(y>=730&&y<800){screen=4;drawer=false;}
                    else if(y<120){drawer=false;}
                    invalidate();
                } else {drawer=false;invalidate();}
                return true;
            }
            if(screen==0 && y<100 && x<120){drawer=true;invalidate();return true;}
            if(screen!=0 && y<100 && x<120){screen=0;invalidate();return true;}
            if(y>getHeight()-90){
                if(x<115) screen=0; else if(x>getWidth()-115) screen=2; else if(x>getWidth()/2+70) screen=1; invalidate(); return true;
            }
            if(screen==0 && y>330 && y<545){ Toast.makeText(MainActivity.this,"Voice mode is ready. Add your AI provider in Settings.",Toast.LENGTH_SHORT).show(); }
            if(screen==4 && y>235&&y<325){ Toast.makeText(MainActivity.this,"Rule editor will be available in the next ADX build.",Toast.LENGTH_SHORT).show(); }
            return true;
        }
    }
}

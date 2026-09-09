package com.adx.assistant;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    ADXView view;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        view = new ADXView();
        setContentView(view);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 20);
        }
    }

    class ADXView extends View {
        Paint p = new Paint(3);
        int screen = 0; // 0 home, 1 memories, 2 chat, 3 settings, 4 rules, 5 menu
        boolean listening = false;
        String[] menu = {"ADX Home","Memories","Chat","Markets","Documents","Website / Coding","Study / Whiteboard","Settings","ADX Rules","PC ↔ Phone","Privacy Policy","About","Notifications"};
        ADXView() { super(MainActivity.this); p.setTypeface(Typeface.create("sans", Typeface.NORMAL)); setBackgroundColor(Color.rgb(7,16,29)); }

        void txt(Canvas c,String s,float x,float y,float size,int color,boolean bold){
            p.setTextSize(size); p.setColor(color); p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));
            c.drawText(s,x,y,p);
        }
        void round(Canvas c,float l,float t,float r,float b,float rad,int color){
            p.setColor(color); c.drawRoundRect(l,t,r,b,rad,rad,p);
        }
        void header(Canvas c,String title, boolean back){
            if(back) txt(c,"‹",35,70,62,Color.WHITE,false);
            else txt(c,"☰",38,66,42,Color.WHITE,false);
            txt(c,title, back?150:135,63,34,Color.WHITE,true);
            txt(c,"♧",655,65,32,Color.WHITE,false);
            round(c,710,28,778,96,20,Color.rgb(18,33,58));
            txt(c,"ADX",722,69,22,Color.rgb(70,170,255),true);
        }
        void card(Canvas c,float l,float t,float r,float b){
            round(c,l,t,r,b,34,Color.rgb(27,37,57));
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(Color.rgb(75,88,112));
            c.drawRoundRect(l,t,r,b,34,34,p); p.setStyle(Paint.Style.FILL);
        }
        void bottom(Canvas c,int active){
            p.setColor(Color.rgb(14,25,43)); c.drawRect(0,getHeight()-150,getWidth(),getHeight(),p);
            String[] a={"⌂","⌾","●","♙","▰"}; String[] b={"Home","Scan","","Memories","Chat"};
            for(int i=0;i<5;i++){
                float x=75+i*175;
                txt(c,a[i],x-18,getHeight()-82,34,i==active?Color.rgb(65,175,240):Color.rgb(120,140,165),false);
                if(!b[i].isEmpty()) txt(c,b[i],x-38,getHeight()-35,19,i==active?Color.rgb(65,175,240):Color.rgb(120,140,165),false);
            }
            round(c,335,getHeight()-175,505,getHeight()-15,80,Color.rgb(60,171,225));
            txt(c,"●",399,getHeight()-70,45,Color.WHITE,false);
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            if(screen==5){ drawMenu(c); return; }
            if(screen==0) drawHome(c);
            else if(screen==1) drawMemories(c);
            else if(screen==2) drawChat(c);
            else if(screen==3) drawSettings(c);
            else if(screen==4) drawRules(c);
        }
        void drawHome(Canvas c){
            header(c,"Friday",false);
            round(c,25,115,795,250,28,Color.rgb(18,36,65));
            txt(c,"Free mode • Personal ADX",55,155,22,Color.WHITE,true);
            txt(c,"Voice, memory, tools and phone actions",55,190,17,Color.rgb(155,173,202),false);
            txt(c,"Activate",620,190,24,Color.rgb(75,140,255),true);
            txt(c,"Good afternoon,",45,330,34,Color.rgb(160,180,210),true);
            txt(c,"ADX",45,385,56,Color.WHITE,true);
            txt(c,"Ready to work.",45,425,25,Color.rgb(150,170,200),false);
            // original assistant avatar placeholder
            round(c,330,450,490,650,70,Color.rgb(18,43,67));
            txt(c,"ADX",367,565,45,Color.rgb(65,175,240),true);
            String[] b={"Focus","Tasks","News"};
            for(int i=0;i<3;i++){ card(c,45+i*250,700,275+i*250,800); txt(c,b[i],105+i*250,760,23,Color.WHITE,true); }
            card(c,45,825,275,955); txt(c,"Weather",70,865,20,Color.rgb(160,180,205),false); txt(c,"—",70,915,30,Color.WHITE,true);
            card(c,300,825,530,955); txt(c,"Today",325,865,20,Color.rgb(160,180,205),false); txt(c,"9",325,915,32,Color.WHITE,true);
            card(c,555,825,785,955); txt(c,"Mood",580,865,20,Color.rgb(160,180,205),false); txt(c,"Focused",580,915,28,Color.WHITE,true);
            round(c,45,985,785,1070,45,Color.rgb(22,34,56)); txt(c,"Ask ADX anything…",95,1038,22,Color.rgb(120,145,175),false); txt(c,"➤",710,1038,28,Color.rgb(100,130,165),false);
            bottom(c,0);
        }
        void drawMemories(Canvas c){
            header(c,"Memories",false);
            txt(c,"Trained tasks",38,150,32,Color.WHITE,true);
            String[] names={"gemini se poochho","chatgpt se poochho","gemini video","gemini image","chatgpt image"};
            String[] sub={"8 steps • sawaal • built-in","8 steps • sawaal • built-in","16 steps • video prompt • built-in","17 steps • image prompt • built-in","14 steps • image prompt • built-in"};
            for(int i=0;i<5;i++){ card(c,35,185+i*125,795,295+i*125); txt(c,names[i],65,235+i*125,27,Color.WHITE,true); txt(c,sub[i],65,270+i*125,17,Color.rgb(55,165,225),false); txt(c,"▶ Play",650,250+i*125,20,Color.rgb(55,165,225),false); }
            card(c,35,825,795,955); txt(c,"Backup & restore",120,875,25,Color.WHITE,true); txt(c,"Move memories and chats to another phone",120,912,17,Color.rgb(155,175,205),false);
            bottom(c,3);
        }
        void drawChat(Canvas c){
            header(c,"Chat",false);
            txt(c,"ADX",35,145,30,Color.WHITE,true);
            round(c,35,180,795,350,30,Color.rgb(18,31,51));
            txt(c,"Hello! I am ADX.",65,225,24,Color.WHITE,true);
            txt(c,"Ask me anything in Hindi or English.",65,265,18,Color.rgb(160,180,205),false);
            round(c,35,900,795,985,45,Color.rgb(22,34,56));
            txt(c,"Type a message…",70,952,21,Color.rgb(120,145,175),false);
            bottom(c,4);
        }
        void drawSettings(Canvas c){
            header(c,"Settings",true);
            int y=115;
            String[][] groups={{"PERSONAL","Personal","Your name, music, AI keys"},{"ASSISTANT","ADX","Persona, voice and language"},{"","Skills","Installed playbooks and tools"},{"","Sub-agents","Coding models and background agents"},{"WORK & MESSAGES,"Email","Send mail from your address"},{"","WhatsApp groups & reports","Your groups and report formats"},{"","Social media","Handle, captions and scheduled posts"},{"CONNECTED ACCOUNTS","Connectors","GitHub, Notion, Telegram and more"},{"MEMORY & DATA","Backup","Export and restore memories and chats"},{"SYSTEM","Advanced","Behaviour, safety, permissions"},{"","Optional","Extra integrations — Maps / Places"}};
            for(String[] g:groups){
                if(!g[0].isEmpty()){ txt(c,g[0],55,y,19,Color.rgb(155,175,205),true); y+=38; }
                card(c,45,y,785,y+105); txt(c,g[1],110,y+45,26,Color.WHITE,true); txt(c,g[2],110,y+78,16,Color.rgb(155,175,205),false); txt(c,"›",735,y+60,38,Color.rgb(120,145,175),false); y+=120;
                if(y>getHeight()-170) break;
            }
        }
        void drawRules(Canvas c){
            header(c,"ADX Rules",true);
            txt(c,"Rules you set for ADX.",38,150,25,Color.rgb(160,180,205),false);
            txt(c,"They guide how your personal assistant behaves.",38,188,19,Color.rgb(160,180,205),false);
            round(c,38,225,790,300,38,Color.rgb(61,171,224)); txt(c,"+ Add rule",335,272,24,Color.WHITE,true);
            txt(c,"No rules yet — ADX is using its defaults.",38,370,20,Color.rgb(160,180,205),false);
        }
        void drawMenu(Canvas c){
            p.setColor(Color.rgb(20,30,49)); c.drawRect(0,0,670,getHeight(),p);
            round(c,0,0,670,getHeight(),0,Color.rgb(20,30,49));
            txt(c,"ADX",55,90,36,Color.WHITE,true); txt(c,"Personal AI Assistant",55,122,20,Color.rgb(155,175,205),false);
            int y=175;
            for(String s:menu){
                if(s.equals("PRODUCTIVITY")||s.equals("SYSTEM")||s.equals("OTHER")) { txt(c,s,55,y,17,Color.rgb(155,175,205),true); y+=45; continue; }
                round(c,28,y-30,640,y+50,40,Color.rgb(31,42,63)); txt(c,s,80,y+18,23,Color.WHITE,false); y+=92;
                if(y>getHeight()-80) break;
            }
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;
            float x=e.getX(), y=e.getY();
            if(screen==0){
                if(y<100 && x<120){ screen=5; }
                else if(y>getHeight()-150 && x<170){screen=0;}
                else if(y>getHeight()-150 && x>500){screen=2;}
                else if(y>getHeight()-150 && x>300&&x<600){screen=1;}
                else if(y>700&&y<820){ /* quick tools */ }
            } else if(screen==1){
                if(y<100&&x<120)screen=5;
                else if(y>getHeight()-160&&x<170)screen=0;
            } else if(screen==2){
                if(y<100&&x<120)screen=5;
            } else if(screen==3||screen==4){
                if(y<100&&x<120)screen=0;
            } else if(screen==5){
                if(x>670){screen=0;}
                else {
                    if(y>145&&y<245)screen=0;
                    else if(y>=245&&y<340)screen=1;
                    else if(y>=340&&y<435)screen=2;
                    else if(y>=650&&y<850)screen=3;
                    else if(y>=850&&y<950)screen=4;
                }
            }
            invalidate(); return true;
        }
    }
}

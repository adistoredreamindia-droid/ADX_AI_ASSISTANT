package com.adx.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.Manifest;
import android.graphics.*;
import android.view.*;

public class MainActivity extends Activity {
  public void onCreate(Bundle b){super.onCreate(b);setContentView(new ADXView()); if(android.os.Build.VERSION.SDK_INT>=23) requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA},10);}
  class ADXView extends View{
    Paint p=new Paint(3); ADXView(){super(MainActivity.this);p.setTypeface(Typeface.DEFAULT);setBackgroundColor(Color.rgb(7,16,29));}
    void t(Canvas c,String s,float x,float y,float z,int col,boolean bold){p.setTextSize(z);p.setColor(col);p.setTypeface(Typeface.create(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x,y,p);}
    void box(Canvas c,float l,float top,float r,float bot,int col){p.setColor(col);c.drawRoundRect(l,top,r,bot,28,28,p);}
    protected void onDraw(Canvas c){super.onDraw(c); t(c,"ADX",40,80,38,Color.WHITE,true);t(c,"Personal AI Assistant",40,118,20,Color.LTGRAY,false);box(c,30,160,810,330,Color.rgb(18,36,65));t(c,"Hello! I am ADX.",60,215,28,Color.WHITE,true);t(c,"Your independent personal assistant.",60,255,19,Color.LTGRAY,false);box(c,30,680,810,770,Color.rgb(22,34,56));t(c,"Ask ADX anything…",65,738,21,Color.rgb(140,160,190),false);t(c,"Chat",45,420,27,Color.WHITE,true);t(c,"Memory",45,490,27,Color.WHITE,true);t(c,"Settings",45,560,27,Color.WHITE,true);}
  }
}

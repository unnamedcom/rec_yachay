package b4a.example;

import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.shellMode) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
		return true;
	}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
		this.setIntent(intent);
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public static eqlib.EQlib _eq = null;
public static anywheresoftware.b4a.phone.Phone.VoiceRecognition _vr = null;
public static anywheresoftware.b4a.obejcts.TTS _tts1 = null;
public static String _aux = "";
public static boolean _recognitiondone = false;
public anywheresoftware.b4a.objects.LabelWrapper _eqb1lbl = null;
public anywheresoftware.b4a.objects.LabelWrapper _eqb2lbl = null;
public anywheresoftware.b4a.objects.LabelWrapper _eqb3lbl = null;
public anywheresoftware.b4a.objects.LabelWrapper _eqb4lbl = null;
public anywheresoftware.b4a.objects.LabelWrapper _eqb5lbl = null;
public anywheresoftware.b4a.objects.SeekBarWrapper _eqband1 = null;
public anywheresoftware.b4a.objects.SeekBarWrapper _eqband2 = null;
public anywheresoftware.b4a.objects.SeekBarWrapper _eqband3 = null;
public anywheresoftware.b4a.objects.SeekBarWrapper _eqband4 = null;
public anywheresoftware.b4a.objects.SeekBarWrapper _eqband5 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _eqbtndone = null;
public anywheresoftware.b4a.objects.ButtonWrapper _eqbtnstart = null;
public anywheresoftware.b4a.objects.LabelWrapper _eqlbl1 = null;
public anywheresoftware.b4a.objects.LabelWrapper _eqlbl2 = null;
public anywheresoftware.b4a.objects.LabelWrapper _eqlbl3 = null;
public anywheresoftware.b4a.objects.LabelWrapper _eqlbl4 = null;
public anywheresoftware.b4a.objects.LabelWrapper _eqlbl5 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper _eqoncb = null;
public anywheresoftware.b4a.objects.PanelWrapper _eqpnl = null;
public anywheresoftware.b4a.objects.PanelWrapper _eqpnlbg = null;
public static short[] _frange = null;
public anywheresoftware.b4a.objects.collections.Map _eqmap = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button1 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edittext1 = null;
public b4a.example.texto _texto = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
vis = vis | (texto.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 57;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 60;BA.debugLine="Activity.LoadLayout(\"EQ.bal\")";
mostCurrent._activity.LoadLayout("EQ.bal",mostCurrent.activityBA);
 //BA.debugLineNum = 62;BA.debugLine="EQPnlBG.SetLayout(0,0,100%x,100%y)";
mostCurrent._eqpnlbg.SetLayout((int) (0),(int) (0),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA));
 //BA.debugLineNum = 64;BA.debugLine="EQPnl.Left=(Activity.Width-EQPnl.Width)/2";
mostCurrent._eqpnl.setLeft((int) ((mostCurrent._activity.getWidth()-mostCurrent._eqpnl.getWidth())/(double)2));
 //BA.debugLineNum = 65;BA.debugLine="EQPnl.Top=(Activity.Height-EQPnl.Height)/2";
mostCurrent._eqpnl.setTop((int) ((mostCurrent._activity.getHeight()-mostCurrent._eqpnl.getHeight())/(double)2));
 //BA.debugLineNum = 67;BA.debugLine="EQMap.Initialize";
mostCurrent._eqmap.Initialize();
 //BA.debugLineNum = 73;BA.debugLine="If FirstTime Then";
if (_firsttime) { 
 //BA.debugLineNum = 74;BA.debugLine="VR.Initialize(\"VR\")";
_vr.Initialize("VR");
 //BA.debugLineNum = 75;BA.debugLine="TTS1.Initialize(\"TTS1\")";
_tts1.Initialize(processBA,"TTS1");
 };
 //BA.debugLineNum = 77;BA.debugLine="If VR.IsSupported Then";
if (_vr.IsSupported()) { 
 //BA.debugLineNum = 78;BA.debugLine="ToastMessageShow(\"Listo para empezar!.\", False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Listo para empezar!.",anywheresoftware.b4a.keywords.Common.False);
 }else {
 //BA.debugLineNum = 80;BA.debugLine="ToastMessageShow(\"El reconocimiento de voz no es soportado por su dispositivo\", True)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("El reconocimiento de voz no es soportado por su dispositivo",anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 82;BA.debugLine="VR.Prompt = \"Bienvenido\"";
_vr.setPrompt("Bienvenido");
 //BA.debugLineNum = 83;BA.debugLine="VR.Language=\"sp\"";
_vr.setLanguage("sp");
 //BA.debugLineNum = 84;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 105;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 106;BA.debugLine="If EQ.IsInitialized Then";
if (_eq.IsInitialized()) { 
 //BA.debugLineNum = 107;BA.debugLine="SaveSettings";
_savesettings();
 //BA.debugLineNum = 108;BA.debugLine="If UserClosed Then EQ.Release";
if (_userclosed) { 
_eq.Release();};
 };
 //BA.debugLineNum = 110;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 95;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 102;BA.debugLine="EQOpen";
_eqopen();
 //BA.debugLineNum = 103;BA.debugLine="End Sub";
return "";
}
public static String  _button1_click() throws Exception{
 //BA.debugLineNum = 236;BA.debugLine="Sub Button1_Click";
 //BA.debugLineNum = 238;BA.debugLine="VR.Listen";
_vr.Listen(processBA);
 //BA.debugLineNum = 239;BA.debugLine="Activity.LoadLayout(\"texto\")";
mostCurrent._activity.LoadLayout("texto",mostCurrent.activityBA);
 //BA.debugLineNum = 240;BA.debugLine="End Sub";
return "";
}
public static String  _eqbtndone_click() throws Exception{
 //BA.debugLineNum = 194;BA.debugLine="Sub EQBtnDone_Click";
 //BA.debugLineNum = 195;BA.debugLine="Activity.Finish";
mostCurrent._activity.Finish();
 //BA.debugLineNum = 196;BA.debugLine="End Sub";
return "";
}
public static String  _eqlevels_valuechanged(int _value,boolean _userchanged) throws Exception{
anywheresoftware.b4a.objects.SeekBarWrapper _send = null;
float _gain = 0f;
 //BA.debugLineNum = 197;BA.debugLine="Sub EQLevels_ValueChanged (Value As Int, UserChanged As Boolean)";
 //BA.debugLineNum = 199;BA.debugLine="Dim Send As SeekBar";
_send = new anywheresoftware.b4a.objects.SeekBarWrapper();
 //BA.debugLineNum = 200;BA.debugLine="Dim Gain As Float";
_gain = 0f;
 //BA.debugLineNum = 201;BA.debugLine="Send = Sender";
_send.setObject((android.widget.SeekBar)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 203;BA.debugLine="Select Send.Tag";
switch (BA.switchObjectToInt(_send.getTag(),(Object)("1"),(Object)("2"),(Object)("3"),(Object)("4"),(Object)("5"))) {
case 0:
 //BA.debugLineNum = 206;BA.debugLine="Gain=Value-FRange(1)";
_gain = (float) (_value-_frange[(int) (1)]);
 //BA.debugLineNum = 207;BA.debugLine="EQB1Lbl.Text=Gain";
mostCurrent._eqb1lbl.setText((Object)(_gain));
 //BA.debugLineNum = 208;BA.debugLine="EQ.SetBandLevel(4,Gain)";
_eq.SetBandLevel((short) (4),(short) (_gain));
 break;
case 1:
 //BA.debugLineNum = 211;BA.debugLine="Gain=Value-FRange(1)";
_gain = (float) (_value-_frange[(int) (1)]);
 //BA.debugLineNum = 212;BA.debugLine="EQB2Lbl.Text=Gain";
mostCurrent._eqb2lbl.setText((Object)(_gain));
 //BA.debugLineNum = 213;BA.debugLine="EQ.SetBandLevel(3,Gain)";
_eq.SetBandLevel((short) (3),(short) (_gain));
 break;
case 2:
 //BA.debugLineNum = 216;BA.debugLine="Gain=Value-FRange(1)";
_gain = (float) (_value-_frange[(int) (1)]);
 //BA.debugLineNum = 217;BA.debugLine="EQB3Lbl.Text=Gain";
mostCurrent._eqb3lbl.setText((Object)(_gain));
 //BA.debugLineNum = 218;BA.debugLine="EQ.SetBandLevel(2,Gain)";
_eq.SetBandLevel((short) (2),(short) (_gain));
 break;
case 3:
 //BA.debugLineNum = 221;BA.debugLine="Gain=Value-FRange(1)";
_gain = (float) (_value-_frange[(int) (1)]);
 //BA.debugLineNum = 222;BA.debugLine="EQB4Lbl.Text=Gain";
mostCurrent._eqb4lbl.setText((Object)(_gain));
 //BA.debugLineNum = 223;BA.debugLine="EQ.SetBandLevel(1,Gain)";
_eq.SetBandLevel((short) (1),(short) (_gain));
 break;
case 4:
 //BA.debugLineNum = 226;BA.debugLine="Gain=Value-FRange(1)";
_gain = (float) (_value-_frange[(int) (1)]);
 //BA.debugLineNum = 227;BA.debugLine="EQB5Lbl.Text=Gain";
mostCurrent._eqb5lbl.setText((Object)(_gain));
 //BA.debugLineNum = 228;BA.debugLine="EQ.SetBandLevel(0,Gain)";
_eq.SetBandLevel((short) (0),(short) (_gain));
 break;
}
;
 //BA.debugLineNum = 233;BA.debugLine="End Sub";
return "";
}
public static String  _eqoncb_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 187;BA.debugLine="Sub EQOnCB_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 188;BA.debugLine="If EQOnCB.Checked Then";
if (mostCurrent._eqoncb.getChecked()) { 
 //BA.debugLineNum = 189;BA.debugLine="EQ.Enable(True)";
_eq.Enable(anywheresoftware.b4a.keywords.Common.True);
 }else {
 //BA.debugLineNum = 191;BA.debugLine="EQ.Enable(False)";
_eq.Enable(anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 193;BA.debugLine="End Sub";
return "";
}
public static String  _eqopen() throws Exception{
 //BA.debugLineNum = 144;BA.debugLine="Sub EQOpen";
 //BA.debugLineNum = 145;BA.debugLine="If EQ.IsAvailable = False Then Return";
if (_eq.IsAvailable()==anywheresoftware.b4a.keywords.Common.False) { 
if (true) return "";};
 //BA.debugLineNum = 147;BA.debugLine="If EQ.IsInitialized = False Then";
if (_eq.IsInitialized()==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 148;BA.debugLine="EQ.Initialize";
_eq.Initialize();
 //BA.debugLineNum = 149;BA.debugLine="Log(EQ.IsInitialized)";
anywheresoftware.b4a.keywords.Common.Log(BA.ObjectToString(_eq.IsInitialized()));
 //BA.debugLineNum = 151;BA.debugLine="If EQ.IsInitialized = False Then Return";
if (_eq.IsInitialized()==anywheresoftware.b4a.keywords.Common.False) { 
if (true) return "";};
 };
 //BA.debugLineNum = 154;BA.debugLine="If EQ.HasControl = False Then";
if (_eq.HasControl()==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 155;BA.debugLine="ToastMessageShow(\"Another App has control of the Equalizer\",False)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow("Another App has control of the Equalizer",anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 156;BA.debugLine="Return";
if (true) return "";
 };
 //BA.debugLineNum = 160;BA.debugLine="EQLbl1.Text = \"1 - \"&(EQ.GetCenterFreq(4)/1000)&\" Hz\"";
mostCurrent._eqlbl1.setText((Object)("1 - "+BA.NumberToString((_eq.GetCenterFreq((short) (4))/(double)1000))+" Hz"));
 //BA.debugLineNum = 161;BA.debugLine="EQLbl2.Text = \"2 - \"&(EQ.GetCenterFreq(3)/1000)&\" Hz\"";
mostCurrent._eqlbl2.setText((Object)("2 - "+BA.NumberToString((_eq.GetCenterFreq((short) (3))/(double)1000))+" Hz"));
 //BA.debugLineNum = 162;BA.debugLine="EQLbl3.Text = \"3 - \"&(EQ.GetCenterFreq(2)/1000)&\" Hz\"";
mostCurrent._eqlbl3.setText((Object)("3 - "+BA.NumberToString((_eq.GetCenterFreq((short) (2))/(double)1000))+" Hz"));
 //BA.debugLineNum = 163;BA.debugLine="EQLbl4.Text = \"4 - \"&(EQ.GetCenterFreq(1)/1000)&\" Hz\"";
mostCurrent._eqlbl4.setText((Object)("4 - "+BA.NumberToString((_eq.GetCenterFreq((short) (1))/(double)1000))+" Hz"));
 //BA.debugLineNum = 164;BA.debugLine="EQLbl5.Text = \"5 - \"&(EQ.GetCenterFreq(0)/1000)&\" Hz\"";
mostCurrent._eqlbl5.setText((Object)("5 - "+BA.NumberToString((_eq.GetCenterFreq((short) (0))/(double)1000))+" Hz"));
 //BA.debugLineNum = 167;BA.debugLine="FRange=EQ.GetBandLevelRange";
_frange = _eq.GetBandLevelRange();
 //BA.debugLineNum = 169;BA.debugLine="EQBand1.Max=FRange(1)*2";
mostCurrent._eqband1.setMax((int) (_frange[(int) (1)]*2));
 //BA.debugLineNum = 170;BA.debugLine="EQBand2.Max=FRange(1)*2";
mostCurrent._eqband2.setMax((int) (_frange[(int) (1)]*2));
 //BA.debugLineNum = 171;BA.debugLine="EQBand3.Max=FRange(1)*2";
mostCurrent._eqband3.setMax((int) (_frange[(int) (1)]*2));
 //BA.debugLineNum = 172;BA.debugLine="EQBand4.Max=FRange(1)*2";
mostCurrent._eqband4.setMax((int) (_frange[(int) (1)]*2));
 //BA.debugLineNum = 173;BA.debugLine="EQBand5.Max=FRange(1)*2";
mostCurrent._eqband5.setMax((int) (_frange[(int) (1)]*2));
 //BA.debugLineNum = 175;BA.debugLine="LoadSettings";
_loadsettings();
 //BA.debugLineNum = 178;BA.debugLine="EQPnlBG.Visible=True";
mostCurrent._eqpnlbg.setVisible(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 180;BA.debugLine="End Sub";
return "";
}
public static String  _eqpnlbg_click() throws Exception{
 //BA.debugLineNum = 181;BA.debugLine="Sub EQPnlBG_Click";
 //BA.debugLineNum = 183;BA.debugLine="End Sub";
return "";
}
public static String  _eqpnlbg_longclick() throws Exception{
 //BA.debugLineNum = 184;BA.debugLine="Sub EQPnlBG_LongClick";
 //BA.debugLineNum = 186;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
texto._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _globals() throws Exception{
 //BA.debugLineNum = 25;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 28;BA.debugLine="Dim RecognitionDone As Boolean = False";
_recognitiondone = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 29;BA.debugLine="Dim EQB1Lbl As Label";
mostCurrent._eqb1lbl = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Dim EQB2Lbl As Label";
mostCurrent._eqb2lbl = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Dim EQB3Lbl As Label";
mostCurrent._eqb3lbl = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Dim EQB4Lbl As Label";
mostCurrent._eqb4lbl = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 33;BA.debugLine="Dim EQB5Lbl As Label";
mostCurrent._eqb5lbl = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 34;BA.debugLine="Dim EQBand1 As SeekBar";
mostCurrent._eqband1 = new anywheresoftware.b4a.objects.SeekBarWrapper();
 //BA.debugLineNum = 35;BA.debugLine="Dim EQBand2 As SeekBar";
mostCurrent._eqband2 = new anywheresoftware.b4a.objects.SeekBarWrapper();
 //BA.debugLineNum = 36;BA.debugLine="Dim EQBand3 As SeekBar";
mostCurrent._eqband3 = new anywheresoftware.b4a.objects.SeekBarWrapper();
 //BA.debugLineNum = 37;BA.debugLine="Dim EQBand4 As SeekBar";
mostCurrent._eqband4 = new anywheresoftware.b4a.objects.SeekBarWrapper();
 //BA.debugLineNum = 38;BA.debugLine="Dim EQBand5 As SeekBar";
mostCurrent._eqband5 = new anywheresoftware.b4a.objects.SeekBarWrapper();
 //BA.debugLineNum = 39;BA.debugLine="Dim EQBtnDone As Button";
mostCurrent._eqbtndone = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 40;BA.debugLine="Dim EQBtnStart As Button";
mostCurrent._eqbtnstart = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 41;BA.debugLine="Dim EQLbl1 As Label";
mostCurrent._eqlbl1 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 42;BA.debugLine="Dim EQLbl2 As Label";
mostCurrent._eqlbl2 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 43;BA.debugLine="Dim EQLbl3 As Label";
mostCurrent._eqlbl3 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 44;BA.debugLine="Dim EQLbl4 As Label";
mostCurrent._eqlbl4 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 45;BA.debugLine="Dim EQLbl5 As Label";
mostCurrent._eqlbl5 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 46;BA.debugLine="Dim EQOnCB As CheckBox";
mostCurrent._eqoncb = new anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper();
 //BA.debugLineNum = 47;BA.debugLine="Dim EQPnl As Panel";
mostCurrent._eqpnl = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 48;BA.debugLine="Dim EQPnlBG As Panel";
mostCurrent._eqpnlbg = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 50;BA.debugLine="Dim FRange() As Short						'Stores the Level range for seek bar processing";
_frange = new short[(int) (0)];
;
 //BA.debugLineNum = 52;BA.debugLine="Dim EQMap As Map";
mostCurrent._eqmap = new anywheresoftware.b4a.objects.collections.Map();
 //BA.debugLineNum = 53;BA.debugLine="Dim Button1 As Button";
mostCurrent._button1 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 54;BA.debugLine="Dim EditText1 As EditText";
mostCurrent._edittext1 = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 55;BA.debugLine="End Sub";
return "";
}
public static String  _loadsettings() throws Exception{
 //BA.debugLineNum = 121;BA.debugLine="Sub LoadSettings";
 //BA.debugLineNum = 123;BA.debugLine="If File.Exists(File.DirDefaultExternal,\"EQSettings.map\") Then";
if (anywheresoftware.b4a.keywords.Common.File.Exists(anywheresoftware.b4a.keywords.Common.File.getDirDefaultExternal(),"EQSettings.map")) { 
 //BA.debugLineNum = 124;BA.debugLine="EQMap=File.ReadMap(File.DirDefaultExternal,\"EQSettings.map\")";
mostCurrent._eqmap = anywheresoftware.b4a.keywords.Common.File.ReadMap(anywheresoftware.b4a.keywords.Common.File.getDirDefaultExternal(),"EQSettings.map");
 //BA.debugLineNum = 127;BA.debugLine="EQBand1.Value=EQMap.Get(\"CurrB1\")+FRange(1)";
mostCurrent._eqband1.setValue((int) ((double)(BA.ObjectToNumber(mostCurrent._eqmap.Get((Object)("CurrB1"))))+_frange[(int) (1)]));
 //BA.debugLineNum = 128;BA.debugLine="EQBand2.Value=EQMap.Get(\"CurrB2\")+FRange(1)";
mostCurrent._eqband2.setValue((int) ((double)(BA.ObjectToNumber(mostCurrent._eqmap.Get((Object)("CurrB2"))))+_frange[(int) (1)]));
 //BA.debugLineNum = 129;BA.debugLine="EQBand3.Value=EQMap.Get(\"CurrB3\")+FRange(1)";
mostCurrent._eqband3.setValue((int) ((double)(BA.ObjectToNumber(mostCurrent._eqmap.Get((Object)("CurrB3"))))+_frange[(int) (1)]));
 //BA.debugLineNum = 130;BA.debugLine="EQBand4.Value=EQMap.Get(\"CurrB4\")+FRange(1)";
mostCurrent._eqband4.setValue((int) ((double)(BA.ObjectToNumber(mostCurrent._eqmap.Get((Object)("CurrB4"))))+_frange[(int) (1)]));
 //BA.debugLineNum = 131;BA.debugLine="EQBand5.Value=EQMap.Get(\"CurrB5\")+FRange(1)";
mostCurrent._eqband5.setValue((int) ((double)(BA.ObjectToNumber(mostCurrent._eqmap.Get((Object)("CurrB5"))))+_frange[(int) (1)]));
 //BA.debugLineNum = 133;BA.debugLine="EQOnCB.Checked=EQMap.Get(\"CurrEnable\")";
mostCurrent._eqoncb.setChecked(BA.ObjectToBoolean(mostCurrent._eqmap.Get((Object)("CurrEnable"))));
 }else {
 //BA.debugLineNum = 136;BA.debugLine="EQBand1.Value=FRange(1)";
mostCurrent._eqband1.setValue((int) (_frange[(int) (1)]));
 //BA.debugLineNum = 137;BA.debugLine="EQBand2.Value=FRange(1)";
mostCurrent._eqband2.setValue((int) (_frange[(int) (1)]));
 //BA.debugLineNum = 138;BA.debugLine="EQBand3.Value=FRange(1)";
mostCurrent._eqband3.setValue((int) (_frange[(int) (1)]));
 //BA.debugLineNum = 139;BA.debugLine="EQBand4.Value=FRange(1)";
mostCurrent._eqband4.setValue((int) (_frange[(int) (1)]));
 //BA.debugLineNum = 140;BA.debugLine="EQBand5.Value=FRange(1)";
mostCurrent._eqband5.setValue((int) (_frange[(int) (1)]));
 };
 //BA.debugLineNum = 142;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 19;BA.debugLine="Dim EQ As EQlib";
_eq = new eqlib.EQlib();
 //BA.debugLineNum = 20;BA.debugLine="Dim VR As VoiceRecognition";
_vr = new anywheresoftware.b4a.phone.Phone.VoiceRecognition();
 //BA.debugLineNum = 21;BA.debugLine="Dim TTS1 As TTS";
_tts1 = new anywheresoftware.b4a.obejcts.TTS();
 //BA.debugLineNum = 22;BA.debugLine="Dim aux As String";
_aux = "";
 //BA.debugLineNum = 23;BA.debugLine="End Sub";
return "";
}
public static String  _savesettings() throws Exception{
 //BA.debugLineNum = 111;BA.debugLine="Sub SaveSettings";
 //BA.debugLineNum = 113;BA.debugLine="EQMap.Put(\"CurrB5\",EQ.GetBandLevel(0))";
mostCurrent._eqmap.Put((Object)("CurrB5"),(Object)(_eq.GetBandLevel((short) (0))));
 //BA.debugLineNum = 114;BA.debugLine="EQMap.Put(\"CurrB4\",EQ.GetBandLevel(1))";
mostCurrent._eqmap.Put((Object)("CurrB4"),(Object)(_eq.GetBandLevel((short) (1))));
 //BA.debugLineNum = 115;BA.debugLine="EQMap.Put(\"CurrB3\",EQ.GetBandLevel(2))";
mostCurrent._eqmap.Put((Object)("CurrB3"),(Object)(_eq.GetBandLevel((short) (2))));
 //BA.debugLineNum = 116;BA.debugLine="EQMap.Put(\"CurrB2\",EQ.GetBandLevel(3))";
mostCurrent._eqmap.Put((Object)("CurrB2"),(Object)(_eq.GetBandLevel((short) (3))));
 //BA.debugLineNum = 117;BA.debugLine="EQMap.Put(\"CurrB1\",EQ.GetBandLevel(4))";
mostCurrent._eqmap.Put((Object)("CurrB1"),(Object)(_eq.GetBandLevel((short) (4))));
 //BA.debugLineNum = 118;BA.debugLine="EQMap.Put(\"CurrEnable\",EQOnCB.Checked)";
mostCurrent._eqmap.Put((Object)("CurrEnable"),(Object)(mostCurrent._eqoncb.getChecked()));
 //BA.debugLineNum = 119;BA.debugLine="File.WriteMap(File.DirDefaultExternal,\"EQSettings.map\",EQMap)";
anywheresoftware.b4a.keywords.Common.File.WriteMap(anywheresoftware.b4a.keywords.Common.File.getDirDefaultExternal(),"EQSettings.map",mostCurrent._eqmap);
 //BA.debugLineNum = 120;BA.debugLine="End Sub";
return "";
}
public static String  _vr_result(boolean _success,anywheresoftware.b4a.objects.collections.List _texts) throws Exception{
 //BA.debugLineNum = 241;BA.debugLine="Sub VR_Result (Success As Boolean, Texts As List)";
 //BA.debugLineNum = 242;BA.debugLine="If Success = True Then";
if (_success==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 243;BA.debugLine="ToastMessageShow(Texts.Get(0), True)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToString(_texts.Get((int) (0))),anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 244;BA.debugLine="TTS1.Speak(Texts.Get(0), True)";
_tts1.Speak(BA.ObjectToString(_texts.Get((int) (0))),anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 245;BA.debugLine="aux=Texts.Get(0)";
_aux = BA.ObjectToString(_texts.Get((int) (0)));
 //BA.debugLineNum = 246;BA.debugLine="EditText1.Text=aux";
mostCurrent._edittext1.setText((Object)(_aux));
 };
 //BA.debugLineNum = 249;BA.debugLine="End Sub";
return "";
}
}

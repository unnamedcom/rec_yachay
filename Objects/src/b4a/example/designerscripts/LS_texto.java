package b4a.example.designerscripts;
import anywheresoftware.b4a.objects.TextViewWrapper;
import anywheresoftware.b4a.objects.ImageViewWrapper;
import anywheresoftware.b4a.BA;


public class LS_texto{

public static void LS_general(java.util.HashMap<String, anywheresoftware.b4a.objects.ViewWrapper<?>> views, int width, int height, float scale) {
anywheresoftware.b4a.keywords.LayoutBuilder.setScaleRate(0.3);
views.get("edittext1").setLeft((int)((5d / 100 * width)));
views.get("edittext1").setWidth((int)((95d / 100 * width) - ((5d / 100 * width))));
//BA.debugLineNum = 4;BA.debugLine="EditText1.SetTopAndBottom(5%y, 95%y)"[texto/General script]
views.get("edittext1").setTop((int)((5d / 100 * height)));
views.get("edittext1").setHeight((int)((95d / 100 * height) - ((5d / 100 * height))));
//BA.debugLineNum = 5;BA.debugLine="Button1.SetLeftAndRight(40%x,60%x)"[texto/General script]
views.get("button1").setLeft((int)((40d / 100 * width)));
views.get("button1").setWidth((int)((60d / 100 * width) - ((40d / 100 * width))));
//BA.debugLineNum = 6;BA.debugLine="Button1.SetTopAndBottom(82%y, 97%y)"[texto/General script]
views.get("button1").setTop((int)((82d / 100 * height)));
views.get("button1").setHeight((int)((97d / 100 * height) - ((82d / 100 * height))));
//BA.debugLineNum = 7;BA.debugLine="ImageView1.SetLeftAndRight(0%x,100%x)"[texto/General script]
views.get("imageview1").setLeft((int)((0d / 100 * width)));
views.get("imageview1").setWidth((int)((100d / 100 * width) - ((0d / 100 * width))));
//BA.debugLineNum = 8;BA.debugLine="ImageView1.SetTopAndBottom(0%y, 100%y)"[texto/General script]
views.get("imageview1").setTop((int)((0d / 100 * height)));
views.get("imageview1").setHeight((int)((100d / 100 * height) - ((0d / 100 * height))));

}
}
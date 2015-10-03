package b4a.example.designerscripts;
import anywheresoftware.b4a.objects.TextViewWrapper;
import anywheresoftware.b4a.objects.ImageViewWrapper;
import anywheresoftware.b4a.BA;


public class LS_eq{

public static void LS_general(java.util.HashMap<String, anywheresoftware.b4a.objects.ViewWrapper<?>> views, int width, int height, float scale) {
anywheresoftware.b4a.keywords.LayoutBuilder.setScaleRate(0.3);
//BA.debugLineNum = 3;BA.debugLine="ImageView1.SetLeftAndRight(0, 100%x)"[eq/General script]
views.get("imageview1").setLeft((int)(0d));
views.get("imageview1").setWidth((int)((100d / 100 * width) - (0d)));
//BA.debugLineNum = 4;BA.debugLine="ImageView1.SetTopAndBottom(0, 100%y)"[eq/General script]
views.get("imageview1").setTop((int)(0d));
views.get("imageview1").setHeight((int)((100d / 100 * height) - (0d)));

}
}
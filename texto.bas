Type=Activity
Version=3
@EndOfDesignText@
#Region  Activity Attributes 
	#FullScreen: False
	#IncludeTitle: False
#End Region

Sub Process_Globals
	'These global variables will be declared once when the application starts.
	'These variables can be accessed from all modules.
	Dim TTS1 As TTS
	Dim aux As String
End Sub

Sub Globals
	'These global variables will be redeclared each time the activity is created.
	'These variables can only be accessed from this module.

	Dim EditText1 As EditText
	Dim Button1 As Button
End Sub

Sub Activity_Create(FirstTime As Boolean)
	'Do not forget to load the layout file created with the visual designer. For example:
	Activity.LoadLayout("texto")
End Sub
Sub VR_Result (Success As Boolean, Texts As List)
	If Success = True Then
		ToastMessageShow(Texts.Get(0), True)
		aux=Main.aux
		EditText1.Text=aux
		TTS1.Speak(Texts.Get(0), True)
	End If
End Sub
Sub Activity_Resume

End Sub

Sub Activity_Pause (UserClosed As Boolean)

End Sub



Sub Button1_Click
	Activity.Finish
	Activity.LoadLayout("eq")
End Sub
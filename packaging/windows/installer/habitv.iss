; Inno Setup script for Habitv (placeholders replaced at build time).
#define MyAppName "Habitv"
#define MyAppVersion "@VERSION@"
#define MyAppPublisher "Habitv"
#define MyAppURL "https://github.com/Mika3578/habitv"
#define MyAppExeName "Habitv.exe"

[Setup]
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={localappdata}\Programs\Habitv
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
OutputDir=@OUTPUT_DIR@
OutputBaseFilename=HabitvSetup
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
UninstallDisplayIcon={app}\bin\{#MyAppExeName}

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "@STAGING_DIR@\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\bin\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\bin\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\bin\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Messages]
SetupAppTitle=Install {#MyAppName}
SetupWindowTitle=Install {#MyAppName} {#MyAppVersion}
WelcomeLabel2=This will install {#MyAppName} {#MyAppVersion} on your computer.%n%nJava 8 with JavaFX support is required. This installer does not bundle a Java runtime.

[Code]
function InitializeSetup: Boolean;
begin
  Result := True;
end;

function JavaAvailable: Boolean;
var
  ResultCode: Integer;
begin
  Result := Exec('cmd.exe', '/c where java', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) and (ResultCode = 0);
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;
  if CurPageID = wpReady then
  begin
    if not JavaAvailable then
    begin
      if MsgBox('Java was not found on PATH. Habitv requires Java 8 with JavaFX support. Continue installation anyway?', mbConfirmation, MB_YESNO) = IDNO then
        Result := False;
    end;
  end;
end;

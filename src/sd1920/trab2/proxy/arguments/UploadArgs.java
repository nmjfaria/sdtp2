package sd1920.trab2.proxy.arguments;

public class UploadArgs {
	final String path, mode;
	final boolean autorename, mute, strict_conflict;

	public UploadArgs(String path, String mode, boolean autorename, boolean mute, boolean strict_conflict) {
		this.path = path;
		this.mode = mode;
		this.autorename = autorename;
		this.mute = mute;
		this.strict_conflict = strict_conflict;
	}
}
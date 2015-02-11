package br.com.livroandroid.carros.backup;

import android.app.backup.FileBackupHelper;
import android.content.Context;

/**
 * Created by Ricardo Lecheta on 10/02/2015.
 */
public class DbBackupHelper extends FileBackupHelper {

    public DbBackupHelper(Context ctx, String dbName) {
        super(ctx, ctx.getDatabasePath(dbName).getAbsolutePath());
    }


}
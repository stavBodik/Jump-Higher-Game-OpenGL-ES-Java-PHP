package com.jump_higher.classes;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * @author Stav Bodik
 *  This class used as "tunnel" between Activity class and Service class , connects and disconnects service to activity . 
 */
public abstract class ServerServiceConnection implements ServiceConnection {
		
		// static instance used in every activity to invoke functions in ServerService class.
		public static ServerService serverService;

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			System.out.println("Connected");
			ServerServiceConnection.serverService = ((ServerService.LocalBinder) binder).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			System.out.println("Disconnected");
			ServerServiceConnection.serverService = null;
		}
		
}
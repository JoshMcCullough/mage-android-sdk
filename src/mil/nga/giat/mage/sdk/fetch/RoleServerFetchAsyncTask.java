package mil.nga.giat.mage.sdk.fetch;

import java.net.URL;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.user.Role;
import mil.nga.giat.mage.sdk.datastore.user.RoleHelper;
import mil.nga.giat.mage.sdk.gson.deserializer.RoleDeserializer;
import mil.nga.giat.mage.sdk.http.client.HttpClientManager;
import mil.nga.giat.mage.sdk.preferences.PreferenceHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Gets roles from server. Does not loop!!! Should be called on initialization
 * to populate the roles and permissions tables.
 * 
 * @author wiedemannse
 * 
 */
public class RoleServerFetchAsyncTask extends ServerFetchAsyncTask {

	private static final String LOG_NAME = RoleServerFetchAsyncTask.class.getName();

	/**
	 * Controls the program flow initialization lifecycle
	 */
	private boolean isInitialization = false;
	
	public RoleServerFetchAsyncTask(Context context) {
		super(context);
	}
	
	public RoleServerFetchAsyncTask(Context context, boolean isInitialization) {
		super(context);
		this.isInitialization = isInitialization;
	}

	@Override
	protected Boolean doInBackground(Object... params) {

		Boolean status = Boolean.TRUE;

		RoleHelper roleHelper = RoleHelper.getInstance(mContext);

		final Gson roleDeserializer = RoleDeserializer.getGsonBuilder();
		DefaultHttpClient httpclient = HttpClientManager.getInstance(mContext).getHttpClient();
		HttpEntity entity = null;
		try {
			URL serverURL = new URL(PreferenceHelper.getInstance(mContext).getValue(R.string.serverURLKey));

			URL roleURL = new URL(serverURL, "api/roles");

			Log.d(LOG_NAME, roleURL.toString());
			HttpGet get = new HttpGet(roleURL.toURI());
			HttpResponse response = httpclient.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				JSONArray json = new JSONArray(EntityUtils.toString(entity));
				if (json != null) {
					for (int i = 0; i < json.length(); i++) {
						JSONObject roleJson = (JSONObject) json.get(i);
						if (roleJson != null) {
							Role role = roleDeserializer.fromJson(roleJson.toString(), Role.class);

							if (role != null) {
								if (roleHelper.read(role.getRemoteId()) == null) {
									role = roleHelper.create(role);
									Log.d(LOG_NAME, "created role with remote_id " + role.getRemoteId());
								}
							} else {
								// ignore updates
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(LOG_NAME, "There was a failure when fetching roles.", e);
			// TODO: should cancel the AsyncTask?
			cancel(Boolean.TRUE);
			status = Boolean.FALSE;
		} finally {
			try {
				if (entity != null) {
					entity.consumeContent();
				}
			} catch (Exception e) {
			}
		}
		return status;
	}
	
	UserServerFetchAsyncTask userTask = null;
	
	@Override
	protected void onPostExecute(Boolean status) {
		super.onPostExecute(status);
		
		if(!status) {
			Log.e(LOG_NAME, "Error getting roles!");
		} else if(isInitialization) {
			// start the next fetching tasks!
			userTask = new UserServerFetchAsyncTask(mContext, true);
			userTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "myself");
		}
	}
	
	public void destroy() {
		cancel(true);
		if(userTask != null) {
			userTask.destroy();
		}
	}
}
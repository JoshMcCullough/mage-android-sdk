package mil.nga.giat.mage.sdk.fetch;

import java.util.List;

import mil.nga.giat.mage.sdk.R;
import mil.nga.giat.mage.sdk.datastore.layer.Layer;
import mil.nga.giat.mage.sdk.datastore.layer.LayerHelper;
import mil.nga.giat.mage.sdk.datastore.staticfeature.StaticFeatureHelper;
import mil.nga.giat.mage.sdk.exceptions.LayerException;
import mil.nga.giat.mage.sdk.exceptions.StaticFeatureException;
import mil.nga.giat.mage.sdk.http.get.MageServerGetRequests;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class StaticFeatureServerFetch extends AbstractServerFetch {

	public StaticFeatureServerFetch(Context context) {
		super(context);
	}

	private static final String LOG_NAME = StaticFeatureServerFetch.class.getName();

	private Boolean isCanceled = Boolean.FALSE;

	public void fetch() {

		StaticFeatureHelper staticFeatureHelper = StaticFeatureHelper.getInstance(mContext);
		LayerHelper layerHelper = LayerHelper.getInstance(mContext);

		Log.d(LOG_NAME, "Pulling static layers.");
		List<Layer> layers = MageServerGetRequests.getLayers(mContext);
		try {
			layerHelper.createAll(layers);

			// set this flag for the layer manager
			Editor sp = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
			sp.putString(mContext.getString(R.string.haveLayersBeenFetchedOnceKey), "true").commit();

			// get ALL the layers
			layers = layerHelper.readAll();

			for (Layer layer : layers) {
				if (isCanceled) {
					break;
				}
				if (layer.getType().equalsIgnoreCase("external")) {
					Log.d(LOG_NAME, "Pulling static features for " + String.valueOf(layer.getName()) + " layer.");
					try {
						staticFeatureHelper.createAll(MageServerGetRequests.getStaticFeatures(mContext, layer));
					} catch (StaticFeatureException e) {
						Log.e(LOG_NAME, "Problem creating static features.", e);
						continue;
					}
				}
			}
		} catch (LayerException e) {
			Log.e(LOG_NAME, "Problem creating layers.", e);
		}
	}

	public void destroy() {
		isCanceled = true;
	}
}

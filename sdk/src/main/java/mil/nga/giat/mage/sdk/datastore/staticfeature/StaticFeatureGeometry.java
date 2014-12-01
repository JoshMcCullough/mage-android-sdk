package mil.nga.giat.mage.sdk.datastore.staticfeature;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vividsolutions.jts.geom.Geometry;

@DatabaseTable(tableName = "staticfeature_geometries")
public class StaticFeatureGeometry {

	@DatabaseField(generatedId = true)
	private Long pk_id;

	@DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
	private Geometry geometry;

	public StaticFeatureGeometry() {
		// ORMLite needs a no-arg constructor
	}

	public StaticFeatureGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public Long getPk_id() {
		return pk_id;
	}

	public void setPk_id(Long pk_id) {
		this.pk_id = pk_id;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
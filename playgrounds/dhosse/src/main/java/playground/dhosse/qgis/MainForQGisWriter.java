package playground.dhosse.qgis;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import playground.dhosse.qgis.layerTemplates.AccessibilityRenderer;
import playground.dhosse.qgis.layerTemplates.NoiseRenderer;
import playground.dhosse.qgis.layerTemplates.SimpleNetworkRenderer;

public class MainForQGisWriter {
	
	public static void main(String args[]){
		
		String workingDirectory =  "C:/Users/Daniel/Desktop/MATSimQGisIntegration/";
		String qGisProjectFile = "testWithAccessibility.qgs";
		
		QGisWriter writer = new QGisWriter(TransformationFactory.WGS84_SA_Albers, workingDirectory);

// ################################################################################################################################################
		
		// use case 1: nodes		
//		QGisLayer nodesLayer = new QGisLayer("nodes", workingDirectory + "nodes.shp", QGisConstants.geometryType.Point);
//		nodesLayer.setRenderer(new SingleSymbolRenderer(nodesLayer.getGeometryType()));
//		nodesLayer.addAttribute("id");
//		writer.addLayer(nodesLayer);
		
// ################################################################################################################################################
		
//		// use case 2: links
//		QGisLayer linksLayer = new QGisLayer("links", workingDirectory + "links.shp", QGisConstants.geometryType.Line);
//		linksLayer.setRenderer(new SingleSymbolRenderer(linksLayer.getGeometryType()));
//		linksLayer.addAttribute("id");
//		linksLayer.addAttribute("length");
//		linksLayer.addAttribute("freespeed");
//		linksLayer.addAttribute("capacity");
//		linksLayer.addAttribute("nlanes");
//		writer.addLayer(linksLayer);
	
// ################################################################################################################################################
		
		// use case 3: noise
//		double[] extent = {4582770.625,5807267.875,4608784.375,5825459.125};
//		writer.setExtent(extent);
//		
//		QGisLayer networkLayer = new QGisLayer("network", "C:/Users/Daniel/Desktop/MATSimQGisIntegration/testFiles/network_detail/network.shp", QGisConstants.geometryType.Line);
//		networkLayer.setRenderer(new SimpleNetworkRenderer(networkLayer.getGeometryType()));
//		writer.addLayer(networkLayer);
//		
//		QGisLayer noiseLayer = new QGisLayer("receiverPoints", "C:/Users/Daniel/Desktop/MATSimQGisIntegration/testFiles/baseCase_rpGap25meters/receiverPoints/receiverPoints.csv",
//				QGisConstants.geometryType.Point);
//		noiseLayer.setDelimiter(";");
//		noiseLayer.setXField("xCoord");
//		noiseLayer.setYField("yCoord");
//		NoiseRenderer renderer = new NoiseRenderer();
//		renderer.setRenderingAttribute("immissions_3600_Immission 11:00:00");
//		noiseLayer.setRenderer(renderer);
//		writer.addLayer(noiseLayer);
//		
//		QGisLayer joinLayer = new QGisLayer("immissions_3600", "C:/Users/Daniel/Desktop/MATSimQGisIntegration/testFiles/baseCase_rpGap25meters/immissions/100.immission_39600.0.csv",
//				QGisConstants.geometryType.No_geometry);
//		writer.addLayer(joinLayer);
//
//		noiseLayer.addVectorJoin(joinLayer, "Receiver Point Id", "receiverPointId");
		
// ################################################################################################################################################
		
		//use case 4: accessibility
		double[] extent = {100000,-3720000,180000,-3675000};
		writer.setExtent(extent);
		
		QGisLayer accessibilityLayer = new QGisLayer("accessibility", "C:/Users/Daniel/Desktop/MATSimQGisIntegration/testFiles/accessibility/accessibilities_header.csv",
				QGisConstants.geometryType.Point);
		accessibilityLayer.setDelimiter(",");
		//there are two ways to set x and y fields for csv geometry files
		//1) if there is a header, you can set the members xField and yField to the name of the column headers
		//2) if there is no header, you can write the column index into the member (e.g. field_1, field_2,...), but works also if there is a header
		accessibilityLayer.setXField("field_1");
		accessibilityLayer.setYField("field_2");
		AccessibilityRenderer renderer = new AccessibilityRenderer();
		renderer.setRenderingAttribute("accessibility"); // choose column/header to visualize
		accessibilityLayer.setRenderer(renderer);
		writer.addLayer(accessibilityLayer);

		writer.write(qGisProjectFile);

	}
	
}
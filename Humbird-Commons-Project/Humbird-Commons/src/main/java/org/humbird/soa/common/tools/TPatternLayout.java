package org.humbird.soa.common.tools;

import org.humbird.soa.common.model.common.MapModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 15/3/17.
 */
public class TPatternLayout implements PatternLayout {

    @Override
    public List<MapModel> conversionPattern(String pattern) {
        List<MapModel> mapModels = new ArrayList<MapModel>();
        for(String v : TString.splitSimpleString(pattern, ' ')) {
            MapModel mapModel = new MapModel(v);
            mapModels.add(mapModel);
        }
        return mapModels;
    }
}

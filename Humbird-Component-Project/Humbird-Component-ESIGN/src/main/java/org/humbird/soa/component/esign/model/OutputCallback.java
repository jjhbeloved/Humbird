package org.humbird.soa.component.esign.model;

import org.humbird.soa.common.model.common.MapModel;

import java.util.UUID;

/**
 * Created by david on 15/4/7.
 */
public class OutputCallback implements MapModel.Callback {

    private int count;

    public OutputCallback(int count) {
        this.count = count;
    }

    public String done(String type, String value) {
        int rang = Integer.parseInt(value);
        String low = null;
        String high = String.valueOf(UUID.randomUUID());
        if (rang > 3) {
            low = String.format("%03d", new Object[]{Integer.valueOf(this.count)});
            rang -= 3;
            if (rang > 0)
                high = high.substring(0, rang);
        } else {
            low = String.format("%05d", new Object[]{Integer.valueOf(this.count)});
            high = high.substring(0, 5);
        }

        return high + low;
    }
}

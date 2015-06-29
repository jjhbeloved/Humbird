package org.humbird.soa.component.esign.model;

/**
 * Created by david on 15/4/7.
 */
public class TimeModel {

    private int deprecatedDelete = 15;

    private int deprecatedUpgrade = 30;

    private byte deprecatedDeleteType = 5;

    private byte deprecatedUpgradeType = 3;

    public TimeModel() {
    }

    public TimeModel(int deprecatedDelete, int deprecatedUpgrade, byte deprecatedDeleteType, byte deprecatedUpgradeType) {
        this.deprecatedDelete = deprecatedDelete;
        this.deprecatedUpgrade = deprecatedUpgrade;
        this.deprecatedDeleteType = deprecatedDeleteType;
        this.deprecatedUpgradeType = deprecatedUpgradeType;
    }

    public int getDeprecatedDelete() {
        return this.deprecatedDelete;
    }

    public void setDeprecatedDelete(int deprecatedDelete) {
        this.deprecatedDelete = deprecatedDelete;
    }

    public int getDeprecatedUpgrade() {
        return this.deprecatedUpgrade;
    }

    public void setDeprecatedUpgrade(int deprecatedUpgrade) {
        this.deprecatedUpgrade = deprecatedUpgrade;
    }

    public byte getDeprecatedDeleteType() {
        return this.deprecatedDeleteType;
    }

    public void setDeprecatedDeleteType(byte deprecatedDeleteType) {
        this.deprecatedDeleteType = deprecatedDeleteType;
    }

    public byte getDeprecatedUpgradeType() {
        return this.deprecatedUpgradeType;
    }

    public void setDeprecatedUpgradeType(byte deprecatedUpgradeType) {
        this.deprecatedUpgradeType = deprecatedUpgradeType;
    }
}

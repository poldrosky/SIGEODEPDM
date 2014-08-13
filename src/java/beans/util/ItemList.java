/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beans.util;

/**
 *
 * @author Omar Ernesto Cabrera Rosero
 */
public class ItemList {

    private String valueSp = "";
    private String valueEn = "";

    public ItemList(String valueSp, String valueEn) {
        this.valueEn = valueEn;
        this.valueSp = valueSp;
    }

    public String getValueSp() {
        return valueSp;
    }

    public void setValueSp(String valueSp) {
        this.valueSp = valueSp;
    }

    public String getValueEn() {
        return valueEn;
    }

    public void setValueEn(String valueEn) {
        this.valueEn = valueEn;
    }
    
    
}

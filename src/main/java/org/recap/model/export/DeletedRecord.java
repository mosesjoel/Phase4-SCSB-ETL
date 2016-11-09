package org.recap.model.export;

import java.io.Serializable;
import java.util.List;

/**
 * Created by premkb on 29/9/16.
 */
public class DeletedRecord implements Serializable{

    private String bibId;

    private List<String> itemIds;

    public String getBibId() {
        return bibId;
    }

    public void setBibId(String bibId) {
        this.bibId = bibId;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }
}

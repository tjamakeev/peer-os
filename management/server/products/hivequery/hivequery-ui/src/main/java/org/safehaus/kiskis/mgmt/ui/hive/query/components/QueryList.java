package org.safehaus.kiskis.mgmt.ui.hive.query.components;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Select;
import org.safehaus.kiskis.mgmt.api.hive.query.Config;
import org.safehaus.kiskis.mgmt.ui.hive.query.HiveQueryUI;

import java.util.List;

/**
 * Created by daralbaev on 11.05.14.
 */
public class QueryList extends ListSelect {

    public static final int ROW_SIZE = 10;

    public QueryList() {

        setImmediate(true);
        setRows(QueryList.ROW_SIZE);

        setSizeFull();

        refreshDataSource(null);
    }

    public void refreshDataSource(String filter) {
        List<Config> list = HiveQueryUI.getManager().load();

        BeanItemContainer<Config> beans =
                new BeanItemContainer<Config>(Config.class);

        for (Config item : list) {
            if (filter == null) {
                beans.addBean(item);
            } else if (item.getName().contains(filter) || item.getDescription().contains(filter)) {
                beans.addBean(item);
            }
        }

        setContainerDataSource(beans);
        setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
        setItemCaptionPropertyId("name");
    }
}

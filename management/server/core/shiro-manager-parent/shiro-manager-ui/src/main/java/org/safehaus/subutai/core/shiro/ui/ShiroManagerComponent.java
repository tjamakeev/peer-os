package org.safehaus.subutai.core.shiro.ui;


import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.shiro.ui.tabs.UserManagementForm;
import org.safehaus.subutai.core.shiro.ui.tabs.UserRegistrationForm;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;


/**
 * Created by talas on 1/21/15.
 */
public class ShiroManagerComponent extends CustomComponent implements Disposable
{

    private UserManagementForm userManagementForm;
    private UserRegistrationForm userRegistrationForm;


    public ShiroManagerComponent( final ShiroManagerPortalModule portalModule )
    {
        setHeight( 100, Unit.PERCENTAGE );

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing( true );
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName( Runo.TABSHEET_SMALL );
        sheet.setSizeFull();
        userManagementForm = new UserManagementForm();
        userRegistrationForm = new UserRegistrationForm();
        userRegistrationForm.init();

        sheet.addTab( userManagementForm, "User management space." );
        sheet.getTab( 0 ).setId( "UserManagement" );

        sheet.addTab( userRegistrationForm, "New user registration space." );
        sheet.getTab( 1 ).setId( "UserRegistration" );
        verticalLayout.addComponent( sheet );


        setCompositionRoot( verticalLayout );
    }


    @Override
    public void dispose()
    {

    }
}

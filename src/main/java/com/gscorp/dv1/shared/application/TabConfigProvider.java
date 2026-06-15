package com.gscorp.dv1.shared.application;

import java.util.List;

import com.gscorp.dv1.shared.TabDto;

public interface TabConfigProvider {

    /**
     * Obtiene todos los tabs disponibles
     */
    List<TabDto> getTabs();
    List<TabDto> getEditTabs();
    /**
     * Obtiene un tab por índice
     */
    TabDto getTabByIndex(int index);
    TabDto getEditTabByIndex(int index);
    
    /**
     * Obtiene un tab por label
     */
    TabDto getTabByLabel(String label);
    TabDto getEditTabByLabel(String label);
    /**
     * Valida si un índice es válido
     */
    boolean isValidTabIndex(int index);
    boolean isValidEditTabIndex(int index);
    /**
     * Obtiene la cantidad total de tabs
     */
    int getTabCount();
    int getEditTabCount();

}

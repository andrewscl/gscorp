package com.gscorp.dv1.hr.employees.application;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gscorp.dv1.shared.TabDto;
import com.gscorp.dv1.shared.application.TabConfigProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeTabsServiceImpl implements TabConfigProvider {

    private static final List<TabDto> EMPLOYEE_TABS = Arrays.asList(
        new TabDto("Personal", "private/employees/fragments/tabs/tab-personal", "user", 0),
        new TabDto("Laboral", "private/employees/fragments/tabs/tab-laboral", "briefcase", 1),
        new TabDto("Financiero", "private/employees/fragments/tabs/tab-finantial", "credit-card", 2)
    );

    // 🚀 Nueva lista inmutable para los componentes de edición
    private static final List<TabDto> EMPLOYEE_EDIT_TABS = List.of(
        new TabDto("Personal", "private/employees/fragments/tabs/tab-personal-edit", "user", 0),
        new TabDto("Laboral", "private/employees/fragments/tabs/tab-laboral-edit", "briefcase", 1),
        new TabDto("Financiero", "private/employees/fragments/tabs/tab-finantial-edit", "credit-card", 2)
    );

    // 🚀 Nueva lista inmutable para los componentes de edición
    private static final List<TabDto> EMPLOYEE_CREATE_TABS = List.of(
        new TabDto("Personal", "private/employees/fragments/tabs/tab-personal-create", "user", 0),
        new TabDto("Laboral", "private/employees/fragments/tabs/tab-laboral-create", "briefcase", 1),
        new TabDto("Financiero", "private/employees/fragments/tabs/tab-finantial-create", "credit-card", 2)
    );

    @Override
    public List<TabDto> getTabs() {
        return EMPLOYEE_TABS;
    }

    @Override
    public List<TabDto> getEditTabs() {
        return EMPLOYEE_EDIT_TABS;
    }

    @Override
    public List<TabDto> getCreateTabs() {
        return EMPLOYEE_CREATE_TABS;
    }

    @Override
    public TabDto getTabByIndex(int index) {
        if (!isValidTabIndex (index)) {
            return null;
        }
        return EMPLOYEE_TABS.get(index);
    }

    @Override
    public TabDto getEditTabByIndex(int index) {
        if (!isValidTabIndex (index)) {
            return null;
        }
        return EMPLOYEE_EDIT_TABS.get(index);
    }

    @Override
    public TabDto getCreateTabByIndex(int index) {
        if (!isValidTabIndex (index)) {
            return null;
        }
        return EMPLOYEE_CREATE_TABS.get(index);
    }

    @Override
    public TabDto getTabByLabel(String label) {
        return EMPLOYEE_TABS.stream()
            .filter(tab -> tab.label().equalsIgnoreCase(label))
            .findFirst()
            .orElse(null);
    }

    @Override
    public TabDto getEditTabByLabel(String label) {
        return EMPLOYEE_EDIT_TABS.stream()
            .filter(tab -> tab.label().equalsIgnoreCase(label))
            .findFirst()
            .orElse(null);
    }

    @Override
    public TabDto getCreateTabByLabel(String label) {
        return EMPLOYEE_CREATE_TABS.stream()
            .filter(tab -> tab.label().equalsIgnoreCase(label))
            .findFirst()
            .orElse(null);
    }

    @Override
    public boolean isValidTabIndex(int index){
        return index >= 0 && index < EMPLOYEE_TABS.size();
    }

    @Override
    public boolean isValidEditTabIndex(int index){
        return index >= 0 && index < EMPLOYEE_EDIT_TABS.size();
    }

    @Override
    public boolean isValidCreateTabIndex(int index){
        return index >= 0 && index < EMPLOYEE_CREATE_TABS.size();
    }

    @Override
    public int getTabCount() {
        return EMPLOYEE_TABS.size();
    }

    @Override
    public int getEditTabCount() {
        return EMPLOYEE_EDIT_TABS.size();
    }

    @Override
    public int getCreateTabCount() {
        return EMPLOYEE_CREATE_TABS.size();
    }

}
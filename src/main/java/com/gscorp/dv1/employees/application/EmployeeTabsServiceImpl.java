package com.gscorp.dv1.employees.application;

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

    @Override
    public List<TabDto> getTabs() {
        return EMPLOYEE_TABS;
    }

    @Override
    public TabDto getTabByIndex(int index) {
        if (!isValidTabIndex (index)) {
            return null;
        }
        return EMPLOYEE_TABS.get(index);
    }

    @Override
    public TabDto getTabByLabel(String label) {
        return EMPLOYEE_TABS.stream()
            .filter(tab -> tab.label().equalsIgnoreCase(label))
            .findFirst()
            .orElse(null);
        }

    @Override
    public boolean isValidTabIndex(int index){
        return index >= 0 && index < EMPLOYEE_TABS.size();
    }

    @Override
    public int getTabCount() {
        return EMPLOYEE_TABS.size();
    }

}
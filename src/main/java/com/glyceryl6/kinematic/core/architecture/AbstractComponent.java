// component/base/AbstractComponent.java
package com.glyceryl6.kinematic.core.architecture;

import com.glyceryl6.kinematic.core.dependency.DependencyInjector;

public abstract class AbstractComponent implements Component {

    protected ComponentContext context;
    protected boolean enabled = true;

    @Override
    public void initialize(ComponentContext context) {
        this.context = context;
        DependencyInjector.injectDependencies(this, context);
        this.onInitialize();
    }

    protected void onInitialize() {}

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void shutdown() {
        this.onShutdown();
    }

    protected void onShutdown() {}

}
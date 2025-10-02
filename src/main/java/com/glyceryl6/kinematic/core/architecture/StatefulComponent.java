package com.glyceryl6.kinematic.core.architecture;

/**
 * 支持状态保存和恢复的组件接口，用于热替换
 */
public interface StatefulComponent extends Component {

    ComponentState saveState();

    void restoreState(ComponentState state);

}
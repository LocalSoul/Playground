package dev.localsoul.playground.ui.layout;

import dev.localsoul.playground.ui.Widget;

import java.util.List;

public interface LayoutStrategy {

    void relayout(Widget widget, List<Widget> children);

}

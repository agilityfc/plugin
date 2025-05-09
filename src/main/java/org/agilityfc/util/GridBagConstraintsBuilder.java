package org.agilityfc.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;

// NOTE: Adapted from
// <https://github.com/anleu/GridBagConstraintsBuilder/blob/master/gridbagconstraintsbuilder/src/com/ztz/gridbagconstraintsbuilder/GridBagContraintsBuilder.java>.

public class GridBagConstraintsBuilder
{
    private final GridBagConstraints init;
    private GridBagConstraints config;

    public GridBagConstraintsBuilder(GridBagConstraints init)
    {
        this.init = init;
        this.config = (GridBagConstraints) init.clone();
    }

    public GridBagConstraintsBuilder()
    {
        this(new GridBagConstraints());
    }

    public GridBagConstraintsBuilder x(int x)
    {
        config.gridx = x;
        return this;
    }

    public GridBagConstraintsBuilder y(int y)
    {
        config.gridy = y;
        return this;
    }

    public GridBagConstraintsBuilder width(int width)
    {
        config.gridwidth = width;
        return this;
    }

    public GridBagConstraintsBuilder height(int height)
    {
        config.gridheight = height;
        return this;
    }

    public GridBagConstraintsBuilder weightX(double weightX)
    {
        config.weightx = weightX;
        return this;
    }

    public GridBagConstraintsBuilder weightY(double weightY)
    {
        config.weighty = weightY;
        return this;
    }

    public GridBagConstraintsBuilder anchor(int anchor)
    {
        config.anchor = anchor;
        return this;
    }

    public GridBagConstraintsBuilder fill(int fill)
    {
        config.fill = fill;
        return this;
    }

    public GridBagConstraintsBuilder insets(int top, int left, int bottom, int right)
    {
        config.insets = new Insets(top, left, bottom, right);
        return this;
    }

    public GridBagConstraintsBuilder insets(Insets insets)
    {
        return insets(insets.top, insets.left, insets.bottom, insets.right);
    }

    /**
     * Set the same inset at top, left, bottom and right.
     */
    public GridBagConstraintsBuilder insets(int inset)
    {
        return insets(inset, inset, inset, inset);
    }

    public GridBagConstraintsBuilder fillNone()
    {
        config.fill = GridBagConstraints.NONE;
        return this;
    }

    public GridBagConstraintsBuilder fillHorizontal()
    {
        config.fill = GridBagConstraints.HORIZONTAL;
        return this;
    }

    public GridBagConstraintsBuilder fillVertical()
    {
        config.fill = GridBagConstraints.VERTICAL;
        return this;
    }

    public GridBagConstraintsBuilder fillBoth()
    {
        config.fill = GridBagConstraints.BOTH;
        return this;
    }

    /**
     * Set fill to GridBagConstraints.HORIZONTAL and horizontal weight to 1.
     */
    public GridBagConstraintsBuilder expandHorizontal()
    {
        config.weightx = 1;
        config.fill = GridBagConstraints.HORIZONTAL;
        return this;
    }

    /**
     * Set fill to GridBagConstraints.VERTICAL and vertical weight to 1.
     */
    public GridBagConstraintsBuilder expandVertical()
    {
        config.weighty = 1;
        config.fill = GridBagConstraints.VERTICAL;
        return this;
    }

    /**
     * Set fill to GridBagConstraints.BOTH and both weights to 1.
     */
    public GridBagConstraintsBuilder expandBoth()
    {
        config.weightx = 1;
        config.weighty = 1;
        config.fill = GridBagConstraints.BOTH;
        return this;
    }

    public GridBagConstraintsBuilder rowRemainder()
    {
        config.gridwidth = GridBagConstraints.REMAINDER;
        return this;
    }

    public GridBagConstraintsBuilder colRemainder()
    {
        config.gridheight = GridBagConstraints.REMAINDER;
        return this;
    }

    public GridBagConstraintsBuilder west()
    {
        config.anchor = GridBagConstraints.WEST;
        return this;
    }

    public GridBagConstraintsBuilder east()
    {
        config.anchor = GridBagConstraints.EAST;
        return this;
    }

    public GridBagConstraintsBuilder north()
    {
        config.anchor = GridBagConstraints.NORTH;
        return this;
    }

    public GridBagConstraintsBuilder northEast()
    {
        config.anchor = GridBagConstraints.NORTHEAST;
        return this;
    }

    public GridBagConstraintsBuilder northWest()
    {
        config.anchor = GridBagConstraints.NORTHWEST;
        return this;
    }

    public GridBagConstraintsBuilder center()
    {
        config.anchor = GridBagConstraints.CENTER;
        return this;
    }

    public GridBagConstraintsBuilder south()
    {
        config.anchor = GridBagConstraints.SOUTH;
        return this;
    }

    public GridBagConstraintsBuilder southEast()
    {
        config.anchor = GridBagConstraints.SOUTHEAST;
        return this;
    }

    public GridBagConstraintsBuilder southWest()
    {
        config.anchor = GridBagConstraints.SOUTHWEST;
        return this;
    }

    public GridBagConstraintsBuilder lineStart()
    {
        config.anchor = GridBagConstraints.LINE_START;
        return this;
    }

    public GridBagConstraintsBuilder lineEnd()
    {
        config.anchor = GridBagConstraints.LINE_END;
        return this;
    }

    public GridBagConstraintsBuilder firstLineStart()
    {
        config.anchor = GridBagConstraints.FIRST_LINE_START;
        return this;
    }

    public GridBagConstraintsBuilder firstLineEnd()
    {
        config.anchor = GridBagConstraints.FIRST_LINE_END;
        return this;
    }

    public GridBagConstraintsBuilder lastLineStart()
    {
        config.anchor = GridBagConstraints.LAST_LINE_START;
        return this;
    }

    public GridBagConstraintsBuilder lastLineEnd()
    {
        config.anchor = GridBagConstraints.LAST_LINE_END;
        return this;
    }

    public GridBagConstraintsBuilder pageStart()
    {
        config.anchor = GridBagConstraints.PAGE_START;
        return this;
    }

    public GridBagConstraintsBuilder pageEnd()
    {
        config.anchor = GridBagConstraints.PAGE_END;
        return this;
    }

    /**
     * Increase the current row position by the current height.
     */
    public GridBagConstraintsBuilder newRow()
    {
        config.gridy += config.gridheight;
        config.gridx = 0;
        return this;
    }

    /**
     * Increase the current column position by the current width.
     */
    public GridBagConstraintsBuilder newCol()
    {
        config.gridx += config.gridwidth;
        return this;
    }

    /**
     * Build the GridBagConstraints and keep the current configuration.
     *
     * @return GridBagConstraints with the given configuration
     */
    public GridBagConstraints build()
    {
        return (GridBagConstraints) config.clone();
    }

    /**
     * Build the GridBagConstraints and reset to the initial configuration.
     *
     * @return GridBagConstraints with the given configuration
     */
    public GridBagConstraints buildAndReset()
    {
        var config = build();
        this.config = (GridBagConstraints) init.clone();
        return config;
    }
}

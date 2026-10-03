package org.agilityfc.util;

import lombok.Getter;
import net.runelite.client.ui.ColorScheme;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// NOTE: Adapted from `net.runelite.client.ui.components.FlatTextField`.

public class FlatTextField extends JPanel
{
    @Getter
    private final JTextField textField;

    //the default background color, this needs to be stored for hover effects
    @Getter
    private Color backgroundColor = ColorScheme.DARKER_GRAY_COLOR;

    //the default hover background color, this needs to be stored for hover effects
    @Getter
    private Color hoverBackgroundColor;

    // the input can be blocked (no clicking, no editing, no hover effects)
    @Getter
    private boolean blocked;

    public FlatTextField()
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 10, 0, 0));

        this.textField = new JTextField();
        this.textField.setBorder(null);
        this.textField.setOpaque(false);
        this.textField.setSelectedTextColor(Color.WHITE);
        this.textField.setSelectionColor(ColorScheme.BRAND_ORANGE_TRANSPARENT);

        add(textField, BorderLayout.CENTER);

        textField.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                if (blocked)
                {
                    return;
                }

                if (hoverBackgroundColor != null)
                {
                    setBackground(hoverBackgroundColor, false);
                }
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                setBackground(backgroundColor);
            }
        });
    }

    public void addActionListener(ActionListener actionListener)
    {
        textField.addActionListener(actionListener);
    }

    public String getText()
    {
        return textField.getText();
    }

    public void setText(String text)
    {
        textField.setText(text);
    }

    @Override
    public void addKeyListener(KeyListener keyListener)
    {
        textField.addKeyListener(keyListener);
    }

    @Override
    public void removeKeyListener(KeyListener keyListener)
    {
        textField.removeKeyListener(keyListener);
    }

    @Override
    public void addFocusListener(FocusListener l)
    {
        textField.addFocusListener(l);
    }

    @Override
    public void removeFocusListener(FocusListener l)
    {
        textField.removeFocusListener(l);
    }

    @Override
    public void setBackground(Color color)
    {
        setBackground(color, true);
    }

    @Override
    public boolean requestFocusInWindow()
    {
        return textField.requestFocusInWindow();
    }

    public void setBackground(Color color, boolean saveColor)
    {
        if (color == null)
        {
            return;
        }

        super.setBackground(color);

        if (saveColor)
        {
            this.backgroundColor = color;
        }
    }

    public void setHoverBackgroundColor(Color color)
    {
        if (color == null)
        {
            return;
        }

        this.hoverBackgroundColor = color;
    }

    public void setEditable(boolean editable)
    {
        this.blocked = !editable;
        textField.setEditable(editable);
        textField.setFocusable(editable);
        if (!editable)
        {
            super.setBackground(backgroundColor);
        }
    }

    public Document getDocument()
    {
        return textField.getDocument();
    }
}

package editor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.MatchResult;

public class TextEditor extends JFrame {

    private final JTextField search;
    private JTextArea textArea;
    private final JFileChooser jfc;
    private boolean isRegex;
    private List<MatchResult> iterator;
    private boolean changed;
    private JCheckBox box;
    private int index;

    public TextEditor() {
        super("The first stage");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);

        this.changed = true;
        this.jfc = new JFileChooser();
        this.jfc.setName("FileChooser");
        add(this.jfc);

        this.search = new JTextField();
        this.search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                TextEditor.this.changed = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                TextEditor.this.changed = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                TextEditor.this.changed = true;
            }
        });
        this.search.setPreferredSize(new Dimension(200, 28));
        this.search.setName("SearchField");

        this.createNorthPanel();

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        menuBar.add(createFileMenu());
        menuBar.add(createSearchMenu());

        createTextAreaWithSlider();
        setVisible(true);
    }

    private void createNorthPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout());
        jPanel.add(this.createSaveButton());
        jPanel.add(this.createLoadButton());
        jPanel.add(this.search);
        jPanel.add(createStartSearchButton());
        jPanel.add(createPreviousMatchButton());
        jPanel.add(createNextMatchButton());
        jPanel.add(this.createREGEXBox());
        jPanel.add(new JLabel("Use regex"));

        add(jPanel, BorderLayout.NORTH);
    }

    private void createTextAreaWithSlider() {
        this.textArea = new JTextArea();
        this.textArea.setName("TextArea");
        JScrollPane scrollableTextArea = new JScrollPane(
                this.textArea);
        scrollableTextArea.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollableTextArea.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollableTextArea.setName("ScrollPane");
        add(scrollableTextArea, BorderLayout.CENTER);
    }

    private JButton createStartSearchButton() {
        JButton searchButton = new JButton(new ImageIcon("search.png"));
        searchButton.setName("StartSearchButton");
        searchButton.addActionListener(action -> {
            try {
                findPattern();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        return searchButton;
    }

    private JButton createLoadButton() {
        JButton loadButton = new JButton(new ImageIcon("load.png"));
        loadButton.setName("OpenButton");
        loadButton.addActionListener(action -> this.loadButtonAction());
        return loadButton;
    }

    private JButton createSaveButton() {
        JButton saveButton = new JButton(new ImageIcon("save.png"));
        saveButton.setName("SaveButton");
        saveButton.addActionListener(action -> this.saveButtonAction());
        return saveButton;
    }

    private JButton createNextMatchButton() {
        JButton nextButton = new JButton(new ImageIcon("right.png"));
        nextButton.setName("NextMatchButton");
        nextButton.addActionListener(action -> nextMatchLogic());
        return nextButton;
    }

    private JButton createPreviousMatchButton() {
        JButton previous = new JButton(new ImageIcon("left.png"));
        previous.setName("PreviousMatchButton");
        previous.addActionListener(action -> previousMatchLogic());
        return previous;
    }

    private JCheckBox createREGEXBox() {
        this.box = new JCheckBox();
        this.box.setName("UseRegExCheckbox");
        this.box.addActionListener(action -> this.changeRegex());
        return this.box;
    }

    private JMenu createSearchMenu() {
        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        searchMenu.setMnemonic(KeyEvent.VK_S);

        JMenuItem search = new JMenuItem("Stat search");
        search.setName("MenuStartSearch");
        search.addActionListener(action -> {
            try {
                this.findPattern();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        JMenuItem prev = new JMenuItem("Previous search");
        prev.setName("MenuPreviousMatch");
        prev.addActionListener(action -> this.previousMatchLogic());

        JMenuItem next = new JMenuItem("Next match");
        next.setName("MenuNextMatch");
        next.addActionListener(action -> this.nextMatchLogic());

        JMenuItem regexp = new JMenuItem("Use regular expressions");
        regexp.setName("MenuUseRegExp");
        regexp.addActionListener(action ->
                this.box.doClick());

        searchMenu.add(search);
        searchMenu.add(prev);
        searchMenu.add(next);
        searchMenu.add(regexp);

        return searchMenu;
    }

    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        menu.setName("MenuFile");
        menu.setMnemonic(KeyEvent.VK_F);

        JMenuItem loadItem = new JMenuItem("Load");
        loadItem.setName("MenuOpen");
        loadItem.addActionListener(action -> this.loadButtonAction());

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setName("MenuSave");
        saveItem.addActionListener(action -> this.saveButtonAction());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setName("MenuExit");
        exitItem.addActionListener(action -> System.exit(0));

        menu.add(saveItem);
        menu.add(loadItem);
        menu.addSeparator();
        menu.add(exitItem);

        return menu;
    }

    private void changeRegex() {
        this.isRegex = !this.isRegex;
    }

    private void previousMatchLogic() {
        if (this.changed) {
            try {
                this.findPattern();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else if (0 <= this.index && this.iterator.size() != 0) {
            this.index = this.index - 1 >= 0 ? this.index - 1 : this.iterator.size() - 1;
            this.grab(this.iterator.get(index));
        }
    }

    private void nextMatchLogic() {
        if (this.changed) {
            try {
                this.findPattern();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else if (this.iterator.size() > this.index && this.iterator.size() != 0) {
            this.index = this.index + 1 != this.iterator.size() ? this.index + 1 : 0;
            this.grab(this.iterator.get(index));
        }
    }

    private void grab(MatchResult match) {
        textArea.setCaretPosition(match.start() + match.group().length());
        textArea.select(match.start(), match.start() + match.group().length());
        textArea.grabFocus();
    }

    private void findPattern() throws ExecutionException, InterruptedException {
        this.changed = false;
        if (!this.search.getText().isEmpty()) {
            PatternSearchWorker psw = new PatternSearchWorker(this.textArea.getText(),
                    this.search.getText(), this.isRegex);

            psw.execute();

            this.iterator = psw.get();
            if (this.iterator.size() != 0) {
                this.index = 0;
                this.grab(this.iterator.get(index));
            }
        }
    }

    private void loadButtonAction() {
        this.jfc.setVisible(true);
        int returnValue = this.jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION &&
                this.jfc.getSelectedFile().exists()) {
            try {
                this.textArea.setText(Files.readString(
                        Paths.get(jfc.getSelectedFile().getAbsolutePath())));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.jfc.setVisible(false);
            }
        } else {
            this.textArea.setText("");
        }
    }

    private void saveButtonAction() {
        this.jfc.setVisible(true);
        int returnValue = this.jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File newFile = new File(this.jfc.getSelectedFile().getAbsolutePath());
            try (FileWriter fileWriter = new FileWriter(newFile)) {
                fileWriter.write(this.textArea.getText());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.jfc.setVisible(false);
            }
        } else {
            System.out.println("Empty field");
        }
    }
}

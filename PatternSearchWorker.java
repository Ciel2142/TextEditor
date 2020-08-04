package editor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternSearchWorker extends SwingWorker<List<MatchResult>, Integer> {
    private final Pattern pa;
    private final boolean isRegex;
    private final String text;
    private final String pattern;

    public PatternSearchWorker(String text, String pattern,
                               boolean isRegex) {
        this.text = text;
        this.pattern = pattern;
        this.isRegex = isRegex;
        this.pa = Pattern.compile(
                pattern, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected List<MatchResult> doInBackground() {
        List<MatchResult> list = new ArrayList<>();
        Matcher matcher = pa.matcher(this.text);
        if (isRegex) {
            while (matcher.find()) {
                list.add(matcher.toMatchResult());
            }
        } else {
            System.out.println(pattern);
            int patternHash = pattern.hashCode();
            for (int i = 0; i + pattern.length() <= this.text.length(); i++) {
                if (patternHash == this.text.substring(i, i + pattern.length()).hashCode()) {
                    int finalStart = i;
                    list.add(new MatchResult() {
                        @Override
                        public int start() {
                            return finalStart;
                        }

                        @Override
                        public int start(int group) {
                            return 0;
                        }

                        @Override
                        public int end() {
                            return 0;
                        }

                        @Override
                        public int end(int group) {
                            return 0;
                        }

                        @Override
                        public String group() {
                            return pattern;
                        }

                        @Override
                        public String group(int group) {
                            return null;
                        }

                        @Override
                        public int groupCount() {
                            return 0;
                        }
                    });
                    i += pattern.length() - 1;
                }
            }
        }
        return list;
    }
}

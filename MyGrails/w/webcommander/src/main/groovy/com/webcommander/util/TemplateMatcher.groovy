package com.webcommander.util

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Santhosh Kumar T
 */
public class TemplateMatcher {
    private Pattern pattern;
    public static boolean STRICT_FORMAT = true;

    public TemplateMatcher(String leftBrace, String rightBrace) {
        leftBrace = Pattern.quote(leftBrace);
        rightBrace = Pattern.quote(rightBrace);
        if(STRICT_FORMAT) {
            pattern = Pattern.compile(leftBrace+"([a-zA-Z\\d_]+)"+rightBrace);
        } else {
            pattern = Pattern.compile(leftBrace+"(.*?)"+rightBrace);
        }
    }

    public TemplateMatcher(String prefix) {
        prefix = Pattern.quote(prefix);
        pattern = Pattern.compile(prefix+"(\\w*)");
    }

    /*-------------------------------------------------[ Replace ]---------------------------------------------------*/
    
    public String replace(CharSequence input, VariableResolver resolver) {
        StringBuilder buff = new StringBuilder();

        Matcher matcher = pattern.matcher(input);
        int cursor = 0;
        while(cursor<input.length() && matcher.find(cursor)){
            buff.append(input.subSequence(cursor, matcher.start()));
            String value = resolver.resolve(matcher.group(1));
            buff.append(value!=null ? value : matcher.group());
            cursor = matcher.end();
        }
        buff.append(input.subSequence(cursor, input.length()));
        return buff.toString();
    }

    public String replace(String input, final Map<String, String> variables) {
        return replace(input, new MapVariableResolver(variables));
    }

    /*-------------------------------------------------[ Character Streams ]---------------------------------------------------*/
    
    public void replace(Reader reader, Writer writer, VariableResolver resolver) throws IOException {
        BufferedReader breader = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
        BufferedWriter bwriter = writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer);
        try{
            boolean firstLine = true;
            for(String line; (line=breader.readLine())!=null;){
                if(firstLine)
                    firstLine = false;
                else
                    bwriter.newLine();
                bwriter.write(replace(line, resolver));
            }
        }finally{
            try{
                breader.close();
            }finally{
                bwriter.close();
            }
        }
    }


    public void replace(Reader reader, Writer writer, Map<String, String> variables) throws IOException {
        replace(reader, writer, new MapVariableResolver(variables));
    }

    /*-------------------------------------------------[ File Copy ]---------------------------------------------------*/
    
    public void copyInto(File source, File targetDir, final VariableResolver resolver) throws IOException {
        replace(new FileReader(source), new FileWriter(new File(targetDir, replace(source.getName(), resolver))), resolver);
    }

    public void copyInto(File source, File targetDir, Map<String, String> variables) throws IOException{
        copyInto(source, targetDir, new MapVariableResolver(variables));
    }

    /*-------------------------------------------------[ VariableResolver ]---------------------------------------------------*/
    
    public static interface VariableResolver{
        public String resolve(String variable);
    }

    public static class MapVariableResolver implements VariableResolver{
        private Map<String, String> variables;

        public MapVariableResolver(Map<String, String> variables){
            this.variables = variables;
        }

        @Override
        public String resolve(String variable){
            return variables.get(variable);
        }
    }
}

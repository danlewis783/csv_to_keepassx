package com.danlewisracing.csvxml;

import com.google.common.collect.Ordering;
import com.google.common.escape.CharEscaper;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.html.HtmlEscapers;
import com.google.common.io.*;
import org.apache.commons.cli.*;
import org.csveed.api.CsvReader;
import org.csveed.api.CsvReaderImpl;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

public class App {
    public static void main(String[] args) throws Exception {
        // create the command line parser
        CommandLineParser parser = new GnuParser();

        // create the Options
        Options options = new Options();

        String description = "the charset to use else use default; use --list-charsets to get the full list";
        options.addOption(OptionBuilder.withLongOpt("charset").withDescription(description).hasArg().create("cs"));
        options.addOption(OptionBuilder.withLongOpt("list-charsets").withDescription("list all charsets").create("listcs"));
        options.addOption(OptionBuilder.withLongOpt("output-file").withDescription("output filename").hasArg().create("out"));
        options.addOption(OptionBuilder.withLongOpt("help").withDescription("usage help").create("h"));

        SortedMap<String, Charset> charsetMap = Charset.availableCharsets();
        Set<String> availCharsets = charsetMap.keySet();

        // parse the command line arguments
        CommandLine line = parser.parse(options, args);
        if (line.hasOption("h")) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("csvxml", options);
            return;
        }

        if (line.hasOption("listcs")) {
            System.out.println("default charset = " + Charset.defaultCharset().name());
            System.out.println("supported charsets");
            System.out.println("=====================");
            for (String s : availCharsets) {
                System.out.println(s);
            }
            return;
        }
        String defaultCharsetName = Charset.defaultCharset().name();
        String charsetName;
        if (line.hasOption("cs")) {
            charsetName = line.getOptionValue("cs");
            if (charsetName == null) {
                throw new IllegalArgumentException("no charset specified");
            }
            if (!availCharsets.contains(charsetName)) {
                throw new IllegalArgumentException("unsupported charset = " + charsetName);
            }
        } else {
            charsetName = defaultCharsetName;
        }
        String outFileName = line.getOptionValue("out", null);

        String[] leftOvers = line.getArgs();
        if (leftOvers == null) {
            throw new NullPointerException("no arguments provided");
        } else if (leftOvers.length == 0) {
            throw new IllegalArgumentException("zero arguments provided");
        } else {
            String fn = leftOvers[0];
            if (fn == null) {
                throw new NullPointerException("first arg null");
            } else if (fn.isEmpty()) {
                throw new IllegalArgumentException("first arg empty");
            } else {
                File f = new File(fn);
                if (!f.exists()) {
                    throw new IOException("file does not exist");
                } else if (f.isDirectory()) {
                    throw new IOException("given filename is a directory, should be a file");
                } else {
                    Charset charset = Charset.forName(charsetName);
                    openFile(f, charset, outFileName);
                }
            }
        }
    }

    static void openFile(File f, Charset cs, String outFileName) throws IOException {
        List<Entry> entries = readWithCsvListReader(Files.asCharSource(f, cs));
        List<Entry> sortedEntries = Ordering.natural().sortedCopy(entries);
        CharSink charSink;
        if (outFileName == null) {
            charSink = new CharSink() {
                @Override
                public Writer openStream() throws IOException {
                    return new PrintWriter(System.out);
                }
            };
        } else {
            charSink = Files.asCharSink(new File(outFileName), Charset.defaultCharset());
        }
        Writer writer = charSink.openBufferedStream();
        writer.write("<!DOCTYPE KEEPASSX_DATABASE>\n" +
                "<database>\n" +
                " <group>\n" +
                "  <title>Internet</title>\n" +
                "  <icon>1</icon>");
        Escaper escaper = HtmlEscapers.htmlEscaper();
        for (Entry entry : sortedEntries) {
            writer.write("<entry>");

            String entryTitle = entry.getTitle();
            if (entryTitle == null) {
                writer.write("<title/>");
            } else {
                writer.write(String.format("<title>%s</title>", escaper.escape(entryTitle)));
            }

            String entryUserName = entry.getUserName();
            if (entryUserName == null) {
                writer.write("<username/>");
            } else {
                writer.write(String.format("<username>%s</username>", escaper.escape(entryUserName)));
            }

            String entryPassword = entry.getPassword();
            if (entryPassword == null) {
                writer.write("<password/>");
            } else {
                writer.write(String.format("<password>%s</password>", escaper.escape(entryPassword)));
            }

            String entryUrl = entry.getUrl();
            if (entryUrl == null) {
                writer.write("<url/>");
            } else {
                writer.write(String.format("<url>%s</url>", escaper.escape(entryUrl)));
            }

            String entryComment = entry.getComment();
            if (entryComment == null) {
                writer.write("<comment/>");
            } else {
                writer.write(String.format("<comment>%s</comment>", escaper.escape(entryComment)));
            }


            writer.write("</entry>\n");
        }
        writer.write("</group>\n" +
                " <group>\n" +
                "  <title>eMail</title>\n" +
                "  <icon>19</icon>\n" +
                " </group>\n" +
                "</database>\n");
        writer.close();
    }

    private static List<Entry> readWithCsvListReader(CharSource source) throws IOException {
        List<Entry> ret = null;
        BufferedReader reader = null;
        try {
            reader = source.openBufferedStream();
            CsvReader<Entry> csvReader = new CsvReaderImpl<Entry>(reader, Entry.class);
            ret = csvReader.readBeans();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return ret;
    }

}

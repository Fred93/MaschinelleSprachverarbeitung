import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.IoTagChunkCodec;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.chunk.TagChunkCodecAdapters;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.tag.LineTaggingParser;
import com.aliasi.tag.Tagging;

import java.io.File;
import java.io.IOException;

public class GeneCorpus
    extends Corpus<ObjectHandler<Chunking>> {

    private final File mConllDataDir;

    public GeneCorpus(File conllMungedDataDir)
        throws IOException {

        mConllDataDir = conllMungedDataDir;

    }
    
    public void setTestFile(String s){
    	TEST_FILE_NAME = s;
    }
    
    public void setTrainFile(String s){
    	TRAIN_FILE_NAME = s;
    }

    public void visitTrain(ObjectHandler<Chunking> handler)
        throws IOException {

        visit(TRAIN_FILE_NAME,handler);
        //visit(DEV_FILE_NAME,handler);
    }


    public void visitTest(ObjectHandler<Chunking> handler) 
        throws IOException {

        visit(TEST_FILE_NAME,handler);
    }

    public void visitTest_untaged(ObjectHandler<Chunking> handler) throws IOException{
    	TagChunkCodec codec
        = new IoTagChunkCodec(); 

    ObjectHandler<Tagging<String>> tagHandler
        = TagChunkCodecAdapters
        .chunkingToTagging(codec,handler);

    
    Parser<ObjectHandler<Tagging<String>>> parser
        = new LineTaggingParser(TOKEN_TAG_LINE_REGEX_TEST,
                                0, 0,
                                IGNORE_LINE_REGEX,
                                EOS_REGEX);
    parser.setHandler(tagHandler);
    File file = new File(mConllDataDir,Test_UNTAGED);
    parser.parse(Test_UNTAGED);
    
    	
    }
    
    private void visit(String fileName, 
                       final ObjectHandler<Chunking> handler)
        throws IOException {

        TagChunkCodec codec
            = new IoTagChunkCodec(); 

        ObjectHandler<Tagging<String>> tagHandler
            = TagChunkCodecAdapters
            .chunkingToTagging(codec,handler);

        if (fileName==TRAIN_FILE_NAME){
         Parser<ObjectHandler<Tagging<String>>> parser
             = new LineTaggingParser(TOKEN_TAG_LINE_REGEX,
                                     TOKEN_GROUP, TAG_GROUP,
                                     IGNORE_LINE_REGEX,
                                     EOS_REGEX);
         parser.setHandler(tagHandler);
         File file = new File(mConllDataDir,fileName);
         parser.parse(file);
        }else{
        Parser<ObjectHandler<Tagging<String>>> parser
            = new LineTaggingParser(TOKEN_TAG_LINE_REGEX,
                                    TOKEN_GROUP, TAG_GROUP,
                                    IGNORE_LINE_REGEX,
                                    EOS_REGEX);
        parser.setHandler(tagHandler);
        File file = new File(mConllDataDir,fileName);
        parser.parse(file);
        }
    }

    static final String TOKEN_TAG_LINE_REGEX
        = "(\\S+)\\s(O|[B|I]-\\S+)"; // token posTag chunkTag entityTag
    static final String TOKEN_TAG_LINE_REGEX_TEST
    = "(\\S+)\\s"; // token posTag chunkTag entityTag

    static final int TOKEN_GROUP = 1; // token
    static final int TAG_GROUP = 2;   // entityTag

    static final String IGNORE_LINE_REGEX
        = "###MEDLINE(.*)";  // lines that start with "##MEDLINE"
     // blank lines end sentences
    static final String EOS_REGEX
        = "\\A\\Z"; 

    static String TRAIN_FILE_NAME = "training5_annotated.iob";
    //static final String DEV_FILE_NAME = "eng.testa";
    static String TEST_FILE_NAME = "training5_annotated.iob";
    static final String Test_UNTAGED = "test5_not_annotated.iob";


}
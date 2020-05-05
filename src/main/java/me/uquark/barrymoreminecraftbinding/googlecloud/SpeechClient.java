package me.uquark.barrymoreminecraftbinding.googlecloud;

import com.google.gson.Gson;
import com.squareup.okhttp.*;

import java.io.IOException;

public class SpeechClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String URL = "https://speech.googleapis.com/v1/speech:recognize";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    static {
        Auth.getAccessToken();
    }

    public static class RecognitionRequest {
        public static class RecognitionConfig {
            public enum AudioEncoding {
                ENCODING_UNSPECIFIED,
                LINEAR16,
                FLAC,
                MULAW,
                AMR,
                AMR_WB,
                OGG_OPUS,
                SPEEX_WITH_HEADER_BYTE
            }

            public static class SpeechContext {
                public String[] phrases = new String[]{};
            }

            public AudioEncoding encoding = AudioEncoding.ENCODING_UNSPECIFIED;
            public int sampleRateHertz = 1;
            public int audioChannelCount;
            public boolean enableSeparateRecognitionPerChannel = false;
            public String languageCode = "en-US";
            public int maxAlternatives = 1;
            public boolean profanityFilter = false;
            public SpeechContext[] speechContexts = new SpeechContext[]{};
            public boolean enableWordTimeOffsets = false;
            public boolean enableAutomaticPunctuation = false;
            public String model = "default";
            public boolean useEnhanced = false;
        }

        public static class RecognitionAudio {
            public String content = "";
        }

        public RecognitionConfig config = new RecognitionConfig();
        public RecognitionAudio audio = new RecognitionAudio();
    }

    public static class RecognitionResponse {
        public static class SpeechRecognitionResult {
            public static class SpeechRecognitionAlternative {
                public static class WordInfo {
                    public String startTime;
                    public String endTime;
                    public String word;
                    public int speakerTag;
                }

                public String transcript;
                public double confidence;
                public WordInfo[] words;
            }

            public SpeechRecognitionAlternative[] alternatives;
            public int channelTag;
        }

        public SpeechRecognitionResult[] results;
    }

    public static RecognitionResponse recognizeRaw(RecognitionRequest params) throws IOException {
        String json = gson.toJson(params);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
            .url(URL)
            .header("Authorization", "Bearer " + Auth.getAccessToken())
            .post(body)
            .build();
        Response response = client.newCall(request).execute();
        if (response.code() != 200)
            return null;
        return gson.fromJson(response.body().string(), RecognitionResponse.class);
    }
}

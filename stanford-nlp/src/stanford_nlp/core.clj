(ns stanford-nlp.core
  (use [clojure.string :only (split triml)])
  (:require [clojure.pprint :refer [pprint]])
  (:require [clj-wordnet.core    :refer :all])
  (:import [edu.stanford.nlp.ling CoreAnnotations$SentencesAnnotation
                                  CoreAnnotations$TokensAnnotation
                                  CoreAnnotations$TextAnnotation
                                  CoreAnnotations$PartOfSpeechAnnotation
                                  CoreAnnotations$NamedEntityTagAnnotation]
           [edu.stanford.nlp.pipeline StanfordCoreNLP Annotation]
           [edu.stanford.nlp.trees TreeCoreAnnotations$TreeAnnotation]
           [edu.stanford.nlp.semgraph SemanticGraphCoreAnnotations$CollapsedCCProcessedDependenciesAnnotation]
           [edu.stanford.nlp.dcoref CorefCoreAnnotations$CorefChainAnnotation]))

(def default-props
  (let [props (java.util.Properties.)]
    ;; POS tagging, lemmatization, NER, parsing, and coreference resolution
    (.put props "annotators" "tokenize, ssplit, pos, lemma, ner, parse, dcoref")
    props))

(defn annotate-text
  "simplifies running all annotators on the document and returning the document itself"
  [text pipeline]
  (let [document (Annotation. text)] ;; create an empty Annotation just with the given text
    (.annotate pipeline document)    ;; run all Annotators on this text
    document))

(defn run-example
  [text]
  (let [props default-props
        pipeline (StanfordCoreNLP. props)
        document (annotate-text text pipeline)
        sentences (.get document CoreAnnotations$SentencesAnnotation)
        graph (.get document CorefCoreAnnotations$CorefChainAnnotation)]
    ; (pprint graph)
    ;; traverses the text; extracts sentences;
    ;; traverses the sentences;
    ;;   - extracts the parse tree of the current sentence;
    ;;   - extracts the Stanford dependency graph of the current sentence
    (doseq [sentence sentences
            :let [tree (.get sentence TreeCoreAnnotations$TreeAnnotation)
                  dependencies (.get sentence SemanticGraphCoreAnnotations$CollapsedCCProcessedDependenciesAnnotation)]]
      (pprint tree)
      (pprint dependencies)
      ;; extracts the tokens from each sentence;
      ;; then categorizes the tokens:
      ;;   - pos = the POS tag of the token
      ;;   - ne = the NER label of the token
      (doseq [token (.get sentence CoreAnnotations$TokensAnnotation)
              :let [word (.get token CoreAnnotations$TextAnnotation)
                    pos (.get token CoreAnnotations$PartOfSpeechAnnotation)
                    ne (.get token CoreAnnotations$NamedEntityTagAnnotation)]]
        (pprint word)
        ; (println (str "-> POS: " pos))
        ; (println (str "-> NER: " ne))
             ))))

(defn -main
  []
  (run-example "I wanted to run but my calf still hurts."))


(def ^:private props
  (doto (java.util.Properties.)
    (.put "annotators" "tokenize, ssplit, pos, lemma, parse")))

(def ^:private pipeline (StanfordCoreNLP. props))

(defn- annotated-doc [s]
   (.process pipeline s))

(annotated-doc "What was that car I saw among the sea of cars stuck in traffic today?")


(StanfordCoreNLP
  (doto (Properties.)
          (.put [annotators tokenize ssplit pos lemma ner parse dcoref])))

(defn blank? [str]
  (every? #(Character/isWhitespace %) str))



(def wordnet (make-dictionary "dict/"))

(def ran (first (wordnet "are" :verb)))

(:lemma ran)

(defn lemmaizer
  "Takes a string of sentences and lemmatizes them"
  [string]
  (map :lemma (map #(first (wordnet %)) (split string #"\s+"))))


(lemmaizer "What does the fox say?")

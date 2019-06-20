import sys
import numpy as np
from sklearn.model_selection import train_test_split

data_directory = sys.argv[1]
label_list = ['outcomes', 'interventions', 'eligibility_criteria', 'trial_design', 'statistical_methods_for_outcome_comparison', 'sample_size_determination'] # sentences with any other labels are labeled "none"
sentence_column = 5 # count starts from 0
label_column = 3

def get_sentence_from_line(line, token_prefix = ''):
    sentence = line.split('|')[sentence_column].lower()
    if token_prefix != '':
        tokens = sentence.split(' ')
        sentence = ' '.join([token_prefix + token for token in tokens])
    return sentence


def get_label_from_line(line):
    return '_'.join(line.split('|')[label_column].lower().split(' '))

def write_chunk_output(text_chunk_in, included_context_sentence_offset, omit_labels):

    sents = []
    labels = []
    chunk_lines = text_chunk_in.splitlines()
    last_line_index = len(chunk_lines) - 1

        # iterate through lines in text chunk
    for chunk_line_number, chunk_line in enumerate(chunk_lines, start=0):

            sentence = get_sentence_from_line(chunk_line)

            context_sentences = []

            # generate context sentences, prepend tokens with
            # specific strings based on context sentence offset
            for sentence_offset in included_context_sentence_offset:
                if not (0 <= chunk_line_number + sentence_offset <= last_line_index):
                    context_sentences.append('no__' + str(sentence_offset))
                else:
                    context_sentences.append(get_sentence_from_line( \
                                                chunk_lines[chunk_line_number+sentence_offset], \
                                                token_prefix = str(sentence_offset) + '__'))



            if len(context_sentences) > 0:
                sentence = "{} {}\n".format( \
                    sentence, ' '.join(context_sentences))

            if omit_labels:
                sents.append(sentence)
            else:
                label = get_label_from_line(chunk_line).lower()
                if label not in label_list:
                    label = 'none'
                sents.append(sentence)
                labels.append(label)
    return sents, labels

def preprocess_corpora(input_filename, included_context_sentence_offset = [], \
                       excluded_chunk_ids = [], omit_labels = False, output_file_postfix = '.preprocessed', split=False):
    fin = open(input_filename,'r')

    output_filename = input_filename + output_file_postfix
    fout = open(output_filename, 'w')

    sents = []
    labels = []
    text_chunk_in = ""
    chunk_id = ''

    # iterate through lines in input file
    for line in fin:
            tokens = line.split('|')
            if chunk_id == '':
                chunk_id = tokens[0]
            if tokens[0] == chunk_id:
                text_chunk_in += line
            else:
                s, l = write_chunk_output(text_chunk_in, included_context_sentence_offset, omit_labels)
                sents += s
                labels += l
                chunk_id = tokens[0]
                text_chunk_in = ""
                text_chunk_in += line

    # process the last abstract
    s,l = write_chunk_output(text_chunk_in, included_context_sentence_offset, omit_labels)
    sents += s
    labels += l
    if not split:
        for i in range(len(sents)):
            fout.write("__label__" + labels[i] + "\t" + sents[i] + "\n")
    else:
        fval = open(data_directory + "valid.txt.preprocessed", "w")
        val_X, test_X, val_Y, test_Y = train_test_split(np.array(sents), np.array(labels), test_size=0.5, stratify=np.array(labels), random_state=227)
        for i in range(len(val_X)):
            fval.write("__label__" + val_Y[i] + "\t" + val_X[i] + "\n")
        for i in range(len(test_X)):
            fout.write("__label__" + test_Y[i] + "\t" + test_X[i] + "\n")
        fval.close()

    fin.close()
    fout.close()

preprocess_corpora(data_directory + 'train.txt')
preprocess_corpora(data_directory + 'test.txt', split=True)

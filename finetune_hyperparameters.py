from __future__ import unicode_literals
import glob, os, sys
import timeit
import itertools
from sklearn.model_selection import ParameterGrid
from sklearn import linear_model
from sklearn import metrics
import pandas as pd
import numpy as np
import subprocess


data_directory = sys.argv[1]
fasttext_binary_path = "fastText-0.1.0/fasttext"
param_grid = {'dim': [10,20,30,50,100], 'wordNgrams': [1,2,3,4], 'epoch': [1,2,3,4,5]}
test_results_df = pd.DataFrame(columns = ['precision', 'recall', 'training_time'] + (list(param_grid.keys())))

def fasttext_train(train_filename, model_filename, params, pretrained_vector_file):
    if pretrained_vector_file:
        subprocess.call(' '.join([fasttext_binary_path, 'supervised', '-input', train_filename, '-output', \
            model_filename, '-pretrainedVectors', pretrained_vector_file, '-dim', str(params['dim']), '-wordNgrams', str(params['wordNgrams']), '-epoch', str(params['epoch']), '-verbose', '0']), shell=True)
        # ! fasttext supervised -input $train_filename -output $model_filename -pretrainedVectors $pretrained_vector_file -dim {params['dim']} -wordNgrams {params['wordNgrams']} -epoch {params['epoch']} -verbose 0
    else:
        subprocess.call(' '.join([fasttext_binary_path, 'supervised', '-input', train_filename, '-output', \
            model_filename, '-dim', str(params['dim']), '-wordNgrams', str(params['wordNgrams']), '-epoch', str(params['epoch']), '-verbose', '1']), shell=True)
        # ! fasttext supervised -input $train_filename -output $model_filename -dim {params['dim']} -wordNgrams {params['wordNgrams']} -epoch {params['epoch']} -verbose 0

def fasttext_test(model_filename, test_filename, plot_matrix = False):
    y_true = []
    with open(test_filename, 'r') as fin:
        for line in fin:
            line = line.decode('utf-8')
            y_true.append(line.split('\t')[0].lower())

    # output = ! fasttext predict $model_filename $test_filename
    process = subprocess.Popen([fasttext_binary_path, 'predict', model_filename, test_filename], stdout=subprocess.PIPE)
    output, err = process.communicate()
    #output = subprocess.check_output(' '.join(['/export/home/pengz3/Desktop/text\ embeddings/fastText-0.1.0/fasttext', 'predict', model_filename, test_filename]), shell=True)
    y_pred = []
    output = output.split(' ')
    output = output[0].split('\n')
    for line in output:
        if not line.isspace() and line:
            y_pred.append(line)

    precision = metrics.precision_score(y_true, y_pred, average='weighted')
    recall = metrics.recall_score(y_true, y_pred, average='weighted')
    f1 = metrics.f1_score(y_true, y_pred, average='weighted')
    print f1

    # if plot_matrix:
    #     labels = ["__label__objective",
    #               "__label__background",
    #               "__label__methods",
    #               "__label__results",
    #               "__label__conclusions"]
    #     # Compute confusion matrix
    #     cnf_matrix = confusion_matrix(y_true, y_pred, labels)
    #     np.set_printoptions(precision=2)
    #     # Plot non-normalized confusion matrix
    #     plt.figure()
    #     plot_confusion_matrix(cnf_matrix, labels=labels, title='Confusion matrix, without normalization')
    #     # Plot normalized confusion matrix
    #     plt.figure()
    #     plot_confusion_matrix(cnf_matrix, labels=labels, normalize=True, title='Normalized confusion matrix')
    #     plt.show()

    return precision, recall, f1


def train_and_test(train_filename, model_filename, test_filename, params, pretrained_vector_file=None, \
                   plot_matrix=False, verbose=False):
    if verbose:
        print("\n" + str(params))

    start_time = timeit.default_timer()
    #print train_filename
    #print model_filename
    fasttext_train(train_filename, model_filename, params, pretrained_vector_file)
    training_time = timeit.default_timer() - start_time

    precision, recall, f1 = fasttext_test(model_filename + '.bin', test_filename, plot_matrix)
    results = {'precision': precision, 'recall': recall, 'f1': f1, 'training_time': training_time}
    if verbose:
        print(str(results))

    return dict(results, **params)

for params in ParameterGrid(param_grid):
    test_results_df = test_results_df.append(train_and_test(data_directory + "train.txt.preprocessed",\
                                "fasttext-model", \
                                data_directory + "valid.txt.preprocessed", params), ignore_index=True)

print(test_results_df.sort_values(['f1', 'training_time'], ascending=[False, True]))

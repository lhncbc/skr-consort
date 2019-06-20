import sklearn.metrics
import numpy as np
import subprocess
import sys

model_filename = sys.argv[1]
test_filename = sys.argv[2]
fasttext_binary_path = "fastText-0.1.0/fasttext"
label_list = ['outcomes', 'interventions', 'eligibility_criteria', 'trial_design', 'statistical_methods_for_outcome_comparison', 'sample_size_determination', 'none'] 
avg = 'macro'

process = subprocess.Popen([fasttext_binary_path, 'predict', model_filename+'.bin', test_filename], stdout=subprocess.PIPE)
output, err = process.communicate()
preds = []
output = output.split(' ')
output = output[0].split('\n')
for line in output:
    if not line.isspace() and line:
        preds.append(line.replace('__label__', '').replace('\n', ''))

targets = []
with open(test_filename, 'r') as f:
    for line in f:
        label = line.split('\t')[0].replace('__label__', '').lower()
        if label in label_list:
            targets.append(label)
        else:
            targets.append('none')

preds = np.array(preds)
targets = np.array(targets)
for label in label_list:
    f1 = sklearn.metrics.f1_score(targets, preds, average=None, labels=label)
    precision = sklearn.metrics.precision_score(targets, preds, average=None, labels=label)
    recall = sklearn.metrics.recall_score(targets, preds, average=None, labels=label)
    print("%s: f1: %.2f pre: %.2f re: %.2f" % (label, f1[0], precision[0], recall[0]))

f1 = sklearn.metrics.f1_score(targets, preds, average=avg)
precision = sklearn.metrics.precision_score(targets, preds, average=avg)
recall = sklearn.metrics.recall_score(targets, preds, average=avg)
print("Overall: f1: %.2f pre: %.2f re: %.2f" % (f1, precision, recall))

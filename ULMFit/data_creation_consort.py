import csv
from sklearn.model_selection import train_test_split
import re
import math
import random

test_file = '../datasets/fasttext_in.txt'
train_file = '../datasets/TrainingDatasets/two_sentence_labels_filtered.txt'
train_file2 = '../datasets/TrainingDatasets/strict_section_based_filtered.txt'
output_dir = 'data/two_sentence_filtered/'


def convertToNumericLabel(textLabel):
    if textLabel == '3a':
        return 1
    if textLabel == '3b':
        return 2
    if textLabel == '4a':
        return 3
    if textLabel == '4b':
        return 4
    if textLabel == '5':
        return 5
    if textLabel == '6a':
        return 6
    if textLabel == '6b':
        return 7
    if textLabel == '7a':
        return 8
    if textLabel == '7b':
        return 9
    if textLabel == '8a':
        return 10
    if textLabel == '8b':
        return 11
    if textLabel == '9':
        return 12
    if textLabel == '10':
        return 13
    if textLabel == '11a':
        return 14
    if textLabel == '11b':
        return 15
    if textLabel == '12a':
        return 16
    if textLabel == '12b':
        return 17
    return 0

labels = []
sentences = []

with open(test_file, 'r') as input:
    for line in input:
        tokens = line.split('|')
        text = tokens[5].replace('\n', '')
        text = re.sub('\s+', ' ', text).strip()
        label = tokens[2]
        if ',' not in label:
            label = convertToNumericLabel(label)
            labels.append(label)
            sentences.append(text)

x_test, x_valid, y_test, y_valid = train_test_split(sentences, labels, random_state=27, test_size=0.5, stratify=labels)

with open(output_dir + 'test.csv', 'w') as output:
    writer = csv.writer(output, delimiter=',', quoting=csv.QUOTE_MINIMAL)
    for i in range(len(x_test)):
        writer.writerow([y_test[i], x_test[i]])

with open(output_dir + 'valid.csv', 'w') as output:
    writer = csv.writer(output, delimiter=',', quoting=csv.QUOTE_MINIMAL)
    for i in range(len(x_valid)):
        writer.writerow([y_valid[i], x_valid[i]])

labels = []
sentences = []
none_sentences = []
count = 0
with open(train_file, 'r') as input:
    for line in input:
        tokens = line.split('|')
        text = tokens[5].replace('\n', '')
        text = re.sub('\s+', ' ', text).strip()
        label = tokens[2]
        if ',' not in label:
            label = convertToNumericLabel(label)
            if label == 0 :
                none_sentences.append(text)
            else:
                labels.append(label)
                sentences.append(text)
                count += 1

with open(train_file2, 'r') as input:
    for line in input:
        tokens = line.split('|')
        text = tokens[5].replace('\n', '')
        text = re.sub('\s+', ' ', text).strip()
        label = tokens[2]
        if ',' not in label:
            label = convertToNumericLabel(label)
            if label == 0 :
                none_sentences.append(text)


none_id = {}
count2 = 0
with open(output_dir + 'train.csv', 'w') as output:
    writer = csv.writer(output, delimiter=',', quoting=csv.QUOTE_MINIMAL)
    for i in range(count):
        writer.writerow([labels[i], sentences[i]])
        id = random.randint(0, len(none_sentences)-1)
        if id not in none_id:
            none_id[id] = 1
            writer.writerow([0, none_sentences[id]])
            count2 += 1

print(count)
print(count2)

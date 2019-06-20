import csv
from sklearn.model_selection import train_test_split
import re
import math
import random

test_file = '../datasets/fasttext_in.txt'
train_file = '../datasets/TrainingDatasets/all_agree_filtered.txt'
train_file2 = '../datasets/TrainingDatasets/95.txt'
output_dir = 'data/all_agree_filtered/'


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
    if label == 'NONE' or label == '0':
        return 0
    return -1

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
count1 = 0
count2 = 0
with open(train_file, 'r') as input:
    for line in input:
        tokens = line.split('|')
        text = tokens[3].replace('\n', '')
        text = re.sub('\s+', ' ', text).strip()
        label = tokens[2]
        if ',' not in label and label != 'NO_AGR':
            label = convertToNumericLabel(label)
            if label == -1:
                print(line)
                sys.exit(0)
            if label == 0:
                if count1 < 5000:
                    ran = random.randint(0, 9)
                    if ran < 3:
                        labels.append(label)
                        sentences.append(text)
                        count1 += 1
            elif label == 16:
                if count2 < 5000:
                    ran = random.randint(0, 9)
                    if ran < 3:
                        labels.append(label)
                        sentences.append(text)
                        count2 += 1
            else:
                labels.append(label)
                sentences.append(text)

with open(train_file2, 'r') as input:
    for line in input:
        tokens = line.split('|')
        text = tokens[2].replace('\n', '')
        text = re.sub('\s+', ' ', text).strip()
        label = tokens[0]
        label = convertToNumericLabel(label)
        if label <= 0:
            print(line)
            sys.exit(0)
        else:
            labels.append(label)
            sentences.append(text)

with open(output_dir + 'train.csv', 'w') as output:
    writer = csv.writer(output, delimiter=',', quoting=csv.QUOTE_MINIMAL)
    for i in range(len(labels)):
        writer.writerow([labels[i], sentences[i]])

print(count1)
print(count2)

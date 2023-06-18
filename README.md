# dlq.clj

Dealing with [Amazon SQS dead-letter-queues](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-dead-letter-queues.html) using [babashka](https://github.com/babashka/babashka).

A dead-letter queue is as of now defined as "SQS queue whose name ends with `-dlq`", but I may change this when I find
a better solution.

## Requirements

- [babashka](https://github.com/babashka/babashka)
- [fzf](https://github.com/junegunn/fzf)

## Features

- `list`: List dead-letter queues and their messages
- `redrive`: Start a redrive-task from one or more DLQs to their origin queues

## Usage

TODO

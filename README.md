# sqs.clj

[![lint](https://github.com/joakimen/sqs.clj/actions/workflows/lint.yml/badge.svg)](https://github.com/joakimen/sqs.clj/actions/workflows/lint.yml)

Dealing with [Amazon SQS queues](https://aws.amazon.com/sqs/) using [babashka](https://github.com/babashka/babashka).

## Requirements

- [babashka](https://github.com/babashka/babashka)
- [fzf](https://github.com/junegunn/fzf)

## Features

- `list`: List queues and their messages
- `redrive`: Start a redrive-task from one or more DLQs to their origin queues

## Install

Install using [bbin](https://github.com/babashka/bbin)

```sh
$ bbin install io.github.joakimen/sqs.clj
{:coords
 {:git/url "https://github.com/joakimen/sqs.clj.git",
  :git/sha "30778e70e6df96cfb7abbd098e774b6da29cce3c"},
 :lib io.github.joakimen/sqs.clj}

```

## AWS Credentials

[sqs.clj](https://github.com/joakimen/sqs.clj) uses AWS credentials from the current shell session.

In order to run with different credentials, assume a different profile using something like [aws-vault](https://github.com/99designs/aws-vault)

## Usage

### Commands

View commands

```sh
$ sqs
Usage: sqs <command>

  sqs list     queues with messages (--all to include empty queues)
  sqs redrive  select one or more queues to redrive messages from (--all to redrive all)
```

### List

List queues and their messages

```sh
$ sqs list
|---------------------------------+----------|
|              Queue              | Messages |
|---------------------------------+----------|
| dev-app1-queue1-dlq             | 62       |
| dev-app1-queue2-dlq             | 46       |
|---------------------------------+----------|
```

### Redrive

Redrive messages from one or more dead-letter queues in parallel and returns their task handles

```sh
$ sqs redrive

# .. user selects one or more queues using fzf

|-----------------------------------------------------------------------+----------|
|                                    Url                                | Messages |
|-----------------------------------------------------------------------+----------|
| https://sqs.eu-west-1.amazonaws.com/123456789012/dev-app1-queue1-dlq  | 46       |
|-----------------------------------------------------------------------+----------|

Press Enter to continue or Ctrl-C to exit

# .. user presses Enter

[ {
  "arn" : "arn:aws:sqs:eu-west-1:123456789012:dev-app1-queue1-dlq",
  "url" : "https://sqs.eu-west-1.amazonaws.com/123456789012/dev-app1-queue1",
  "task-handle" : "uOJ0yXNrSWQiOiJlODE8FjE4MS0xMzJjLTQxZjYtODdmZS0yMzAwZTVlMWJmYjcaLCJzb3VyY2VBcm4iOiJhcm46YXdzOnNxczpldS13ZXN0LTE6ODQ5MTM4MjY3Mzg5OmV1cm9wcmlzLWRldi1xcm9kDWN0LWludGVybmFsLWRscSJ9"
} ]

```

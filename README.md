# Coevolution
Coevolution in Task Scheduling using Genetic Algorithms

We introduce coevolution to the scheduling problem. We will do this in order to see how effective co-evolution is compared to known entities. The input to our problem is a list of preferences one for every monitor. Each preference list is an array where in each position the monitor gives availability from 1-5 with 1 being most preferred and 5 being the least, meaning that they cannot work during that time slot. The available time slots are equally distributed among all the monitors. The goal is to assign the time slots to the monitors such that most time slots are assigned according to their preferences, at the same time making sure that every monitor is assigned equal number of slots.

By introducing co-evolution to this familiar task scheduling problem, we apply the proposed solution with coevolution on the available test data to understand how efficient coevolution is when compared to the standard methods used. We introduce a new fitness function with two
significant parts.

1. Coevolution Term : Each individual is compared with every other individual in the population to determine if an individual is retained in the next generation.

2. Slot Preference : We compare the slot assignments with the preferences of the monitors to
calculate the fitness of an individual.

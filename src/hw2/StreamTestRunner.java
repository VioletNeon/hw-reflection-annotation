package hw2;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StreamTestRunner {
    record Employee(String name, int age, String title) {}

    public static void run() {
        List<Integer> numbs = List.of(5, 2, 10, 9, 4, 3, 10, 1, 13);

        List<Integer> topThree = numbs.stream()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .toList();

        System.out.println("Result of task 1: " + topThree);

        List<Integer> topThreeWithoutDouble = numbs.stream()
                .sorted(Comparator.reverseOrder())
                .distinct()
                .limit(3)
                .toList();

        System.out.println("Result of task 2: " + topThreeWithoutDouble);

        List<Employee> employees = List.of(
                new Employee("Александр", 27, "Инженер"),
                new Employee("Владимир", 39, "Эколог"),
                new Employee("Светлана", 35, "Инженер"),
                new Employee("Анна", 29, "Юрист"),
                new Employee("Андрей", 30, "Инженер"),
                new Employee("Екатерина", 26, "Экономист")
        );

        List<Employee> topThreeOfEmployeesByTitleAndAge = employees.stream()
                .filter(it -> it.title.equals("Инженер"))
                .sorted(Comparator.comparingInt(Employee::age).reversed())
                .limit(3)
                .toList();

        System.out.println("Result of task 3: " + topThreeOfEmployeesByTitleAndAge);

        double averageOfEmployeesByAge = employees.stream()
                .filter(it -> it.title.equals("Инженер"))
                .collect(Collectors.averagingDouble(Employee::age));

        System.out.println("Result of task 4: " + Math.round(averageOfEmployeesByAge));

        List<String> words = List.of(
                "Апельсин",
                "Личи",
                "Слива",
                "Виноград",
                "Дыня",
                "Земляника"
        );

        Optional<String> theLongestWord = words.stream()
                .max(Comparator.comparingInt(String::length));

        System.out.println("Result of task 5: " + theLongestWord);

        String sentence = "солнце светит ярко очень ярко люблю лето люблю когда солнце согревает землю летом тепло и тепло чувствуется везде";

        Map<String, Integer> repeatedWords = Arrays.stream(sentence.split(" "))
                .collect(Collectors.toMap(
                        Function.identity(),
                        w -> 1,
                        Integer::sum
                        ));

        System.out.println("Result of task 6: " + repeatedWords);

        List<String> sortedWordsByLength = words.stream()
                .sorted(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()))
                .toList();

        System.out.println("Result of task 7*: " + sortedWordsByLength);

        List<String> sentences = List.of(
                "оранжевый апельсин очень ярко пахнет",
                "шершавый личи внутри очень сочный",
                "незрелая слива имеет кислый вкус",
                "виноград бывает зеленый или темный",
                "дыня вкуснее чем арбуз уверяю",
                "земляника как правило меньше клубники"
        );

        Optional<String> theLongestWordInSentences = sentences.stream()
                .flatMap((it) -> Arrays.stream(it.split(" ")))
                .max(Comparator.comparingInt(String::length));

        System.out.println("Result of task 8*: " + theLongestWordInSentences);
    }

    public static void main(String[] args) throws Exception {
        run();
    }
}

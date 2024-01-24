package projekt;

import org.sourcegrade.jagr.api.rubric.*;

public class Projekt_RubricProvider implements RubricProvider {

    @Override
    public Rubric getRubric() {
        return Rubric.builder()
            .title("Projekt")
            .addChildCriteria(Criterion.builder()
                .shortDescription("Test")
                .grader(Grader.testAwareBuilder()
                    .requirePass(JUnitTestRef.ofMethod(() -> SanityCheck.class.getDeclaredMethod("test", SanityCheck.ClassRecord.class)))
                    .pointsFailedMin()
                    .pointsPassedMax()
                    .build())
                .build())
            .build();
    }
}

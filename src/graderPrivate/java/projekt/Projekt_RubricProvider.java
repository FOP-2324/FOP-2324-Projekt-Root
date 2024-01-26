package projekt;

import org.sourcegrade.jagr.api.rubric.*;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;

public class Projekt_RubricProvider implements RubricProvider {

    @Override
    public Rubric getRubric() {
        return Rubric.builder()
            .title("Projekt")
            .addChildCriteria(
                Criterion.builder()
                    .shortDescription("PlayerImpl")
                    .addChildCriteria(
                        Criterion.builder()
                            .shortDescription("getResources() funktioniert wie beschrieben")
                            .grader(Grader.testAwareBuilder()
                                .requirePass(JUnitTestRef.ofMethod(() -> PlayerImplTest.InventorySystem.class.getDeclaredMethod("testGetResources")))
                                .pointsFailedMin()
                                .pointsPassedMax()
                                .build())
                            .build(),
                        Criterion.builder()
                            .shortDescription("addResource(ResourceType, int) funktioniert wie beschrieben")
                            .grader(Grader.testAwareBuilder()
                                .requirePass(JUnitTestRef.ofMethod(() -> PlayerImplTest.InventorySystem.class.getDeclaredMethod("testAddResource", JsonParameterSet.class)))
                                .pointsFailedMin()
                                .pointsPassedMax()
                                .build())
                            .build(),
                        Criterion.builder()
                            .shortDescription("removeResource(ResourceType, int) funktioniert wie beschrieben")
                            .grader(Grader.testAwareBuilder()
                                .requirePass(JUnitTestRef.ofMethod(() -> PlayerImplTest.InventorySystem.class.getDeclaredMethod("testRemoveResource", JsonParameterSet.class)))
                                .pointsFailedMin()
                                .pointsPassedMax()
                                .build())
                            .build(),
                        Criterion.builder()
                            .shortDescription("removeResources(Map<ResourceType, Integer>) funktioniert wie beschrieben")
                            .grader(Grader.testAwareBuilder()
                                .requirePass(JUnitTestRef.ofMethod(() -> PlayerImplTest.InventorySystem.class.getDeclaredMethod("testRemoveResources", JsonParameterSet.class)))
                                .pointsFailedMin()
                                .pointsPassedMax()
                                .build())
                            .build(),
                        Criterion.builder()
                            .shortDescription("hasResources(Map<ResourceType, Integer>) funktioniert wie beschrieben")
                            .grader(Grader.testAwareBuilder()
                                .requirePass(JUnitTestRef.ofMethod(() -> PlayerImplTest.InventorySystem.class.getDeclaredMethod("testHasResources", JsonParameterSet.class)))
                                .pointsFailedMin()
                                .pointsPassedMax()
                                .build())
                            .build(),
                        Criterion.builder()
                            .shortDescription("getTradeRatio(ResourceType) funktioniert wie beschrieben")
                            .grader(Grader.testAwareBuilder()
                                .requirePass(JUnitTestRef.ofMethod(() -> PlayerImplTest.class.getDeclaredMethod("testGetTradeRatio")))
                                .pointsFailedMin()
                                .pointsPassedMax()
                                .build())
                            .build())
                    .build(),
                Criterion.builder()
                    .shortDescription("Misc")
                    .addChildCriteria(Criterion.builder()
                        .shortDescription("Sanity Check")
                        .grader(Grader.testAwareBuilder()
                            .requirePass(JUnitTestRef.ofMethod(() -> SanityCheck.class.getDeclaredMethod("test", JsonParameterSet.class)))
                            .pointsFailedMin()
                            .pointsPassedMax()
                            .build())
                        .build())
                    .build()
            )
            .build();
    }
}

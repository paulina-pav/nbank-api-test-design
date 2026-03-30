package db.models.comparison;

import api.models.BaseModel;
import db.models.BaseDaoModel;
import org.assertj.core.api.AbstractAssert;

public class DaoAndModelAssertions {
    private static final DaoComparator daoComparator = new DaoComparator();

    public static DaoModelAssert assertThat(BaseModel apiModel, BaseDaoModel daoModel) {
        return new DaoModelAssert(apiModel, daoModel);
    }

    public static class DaoModelAssert extends AbstractAssert<DaoModelAssert, BaseDaoModel> {
        private final BaseModel apiModel;
        private final BaseDaoModel daoModel;

        public DaoModelAssert(BaseModel apiModel, BaseDaoModel daoModel) {
            super(daoModel, DaoModelAssert.class);
            this.apiModel = apiModel;
            this.daoModel = daoModel;
        }

        public DaoModelAssert match() {
            if (apiModel == null) {
                failWithMessage("API model should not be null");
            }

            if (daoModel == null) {
                failWithMessage("DAO model should not be null");
            }

            // Use configurable comparison
            try {
                daoComparator.compare(apiModel, daoModel);
            } catch (AssertionError e) {
                failWithMessage(e.getMessage());
            }

            return this;
        }
    }
}

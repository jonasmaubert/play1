package play.db.jpa;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

//Explicit SAVE for JPABase is implemented here
// ~~~~~~
// We've hacked the org.hibernate.event.def.AbstractFlushingEventListener line 271, to flush collection update,remove,recreation
// only if the owner will be saved or if the targeted entity will be saved (avoid the org.hibernate.HibernateException: Found two representations of same collection)
// As is:
// if (session.getInterceptor().onCollectionUpdate(coll, ce.getLoadedKey())) {
//      actionQueue.addAction(...);
// }
//
// This is really hacky. We should move to something better than Hibernate like EBEAN
private class HibernateInterceptor extends EmptyInterceptor {

    public HibernateInterceptor() {

    }

    @Override
    public int[] findDirty(Object o, Serializable id, Object[] arg2, Object[] arg3, String[] arg4, Type[] arg5) {
        if (o instanceof JPABase && !((JPABase) o).willBeSaved) {
            return new int[0];
        }
        return null;
    }

    @Override
    public boolean onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
        if (collection instanceof PersistentCollection) {
            Object o = ((PersistentCollection) collection).getOwner();
            if (o instanceof JPABase) {
                if (entities.get() != null) {
                    return ((JPABase) o).willBeSaved || ((JPABase) entities.get()).willBeSaved;
                } else {
                    return ((JPABase) o).willBeSaved;
                }
            }
        } else {
            System.out.println("HOO: Case not handled !!!");
        }
        return super.onCollectionUpdate(collection, key);
    }

    @Override
    public boolean onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
        if (collection instanceof PersistentCollection) {
            Object o = ((PersistentCollection) collection).getOwner();
            if (o instanceof JPABase) {
                if (entities.get() != null) {
                    return ((JPABase) o).willBeSaved || ((JPABase) entities.get()).willBeSaved;
                } else {
                    return ((JPABase) o).willBeSaved;
                }
            }
        } else {
            System.out.println("HOO: Case not handled !!!");
        }
        return super.onCollectionRecreate(collection, key);
    }

    @Override
    public boolean onCollectionRemove(Object collection, Serializable key) throws CallbackException {
        if (collection instanceof PersistentCollection) {
            Object o = ((PersistentCollection) collection).getOwner();
            if (o instanceof JPABase) {
                if (entities.get() != null) {
                    return ((JPABase) o).willBeSaved || ((JPABase) entities.get()).willBeSaved;
                } else {
                    return ((JPABase) o).willBeSaved;
                }
            }
        } else {
            System.out.println("HOO: Case not handled !!!");
        }
        return super.onCollectionRemove(collection, key);
    }

    protected ThreadLocal<Object> entities = new ThreadLocal<Object>();

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        entities.set(entity);
        return super.onSave(entity, id, state, propertyNames, types);
    }

    @Override
    public void afterTransactionCompletion(org.hibernate.Transaction tx) {
        entities.remove();
    }

}

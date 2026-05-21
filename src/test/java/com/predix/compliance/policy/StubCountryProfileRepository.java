package com.predix.compliance.policy;

import com.predix.compliance.domain.CountryProfileEntity;
import com.predix.compliance.repository.CountryProfileRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class StubCountryProfileRepository implements CountryProfileRepository {

    private final Map<String, CountryProfileEntity> store = new HashMap<>();

    void put(CountryProfileEntity entity) {
        store.put(entity.getCountryCode(), entity);
    }

    @Override
    public Optional<CountryProfileEntity> findById(String countryCode) {
        return Optional.ofNullable(store.get(countryCode));
    }

    @Override
    public List<CountryProfileEntity> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public <S extends CountryProfileEntity> S save(S entity) {
        store.put(entity.getCountryCode(), entity);
        return entity;
    }

    @Override
    public <S extends CountryProfileEntity> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean existsById(String s) {
        return store.containsKey(s);
    }

    @Override
    public long count() {
        return store.size();
    }

    @Override
    public void deleteById(String s) {
        store.remove(s);
    }

    @Override
    public void delete(CountryProfileEntity entity) {
        store.remove(entity.getCountryCode());
    }

    @Override
    public void deleteAllById(Iterable<? extends String> strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(Iterable<? extends CountryProfileEntity> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    @Override
    public List<CountryProfileEntity> findAll(Sort sort) {
        return findAll();
    }

    @Override
    public Page<CountryProfileEntity> findAll(Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CountryProfileEntity> findAllById(Iterable<String> strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends CountryProfileEntity> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends CountryProfileEntity> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends CountryProfileEntity> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends CountryProfileEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends CountryProfileEntity> long count(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends CountryProfileEntity> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends CountryProfileEntity, R> R findBy(Example<S> example,
                                                        Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() {
    }

    @Override
    public <S extends CountryProfileEntity> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends CountryProfileEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllInBatch(Iterable<CountryProfileEntity> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<String> strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllInBatch() {
        store.clear();
    }

    @Override
    public CountryProfileEntity getOne(String s) {
        return findById(s).orElseThrow();
    }

    @Override
    public CountryProfileEntity getById(String s) {
        return getOne(s);
    }

    @Override
    public CountryProfileEntity getReferenceById(String s) {
        return getOne(s);
    }
}

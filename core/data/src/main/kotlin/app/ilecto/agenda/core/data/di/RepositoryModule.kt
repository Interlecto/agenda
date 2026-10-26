package app.ilecto.agenda.core.data.di

import app.ilecto.agenda.core.common.DefaultDispatcherProvider
import app.ilecto.agenda.core.common.DispatcherProvider
import app.ilecto.agenda.core.data.repository.AccountRepositoryImpl
import app.ilecto.agenda.core.data.repository.CalendarRepositoryImpl
import app.ilecto.agenda.core.domain.repository.AccountRepository
import app.ilecto.agenda.core.domain.repository.CalendarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}

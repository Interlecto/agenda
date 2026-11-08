package app.ilecto.agenda.core.data.di

import app.ilecto.agenda.core.common.DefaultDispatcherProvider
import app.ilecto.agenda.core.common.DispatcherProvider
import app.ilecto.agenda.core.data.mail.MailScanEngineImpl
import app.ilecto.agenda.core.data.repository.AccountCalendarRepositoryImpl
import app.ilecto.agenda.core.data.repository.AccountRepositoryImpl
import app.ilecto.agenda.core.data.repository.CalendarRepositoryImpl
import app.ilecto.agenda.core.data.repository.MailSuggestionRepositoryImpl
import app.ilecto.agenda.core.data.repository.SyncRuleRepositoryImpl
import app.ilecto.agenda.core.data.sync.SyncEngineImpl
import app.ilecto.agenda.core.domain.mail.MailScanEngine
import app.ilecto.agenda.core.domain.repository.AccountCalendarRepository
import app.ilecto.agenda.core.domain.repository.AccountRepository
import app.ilecto.agenda.core.domain.repository.CalendarRepository
import app.ilecto.agenda.core.domain.repository.MailSuggestionRepository
import app.ilecto.agenda.core.domain.repository.SyncRuleRepository
import app.ilecto.agenda.core.domain.sync.SyncEngine
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
    abstract fun bindSyncRuleRepository(impl: SyncRuleRepositoryImpl): SyncRuleRepository

    @Binds
    @Singleton
    abstract fun bindAccountCalendarRepository(impl: AccountCalendarRepositoryImpl): AccountCalendarRepository

    @Binds
    @Singleton
    abstract fun bindSyncEngine(impl: SyncEngineImpl): SyncEngine

    @Binds
    @Singleton
    abstract fun bindMailSuggestionRepository(impl: MailSuggestionRepositoryImpl): MailSuggestionRepository

    @Binds
    @Singleton
    abstract fun bindMailScanEngine(impl: MailScanEngineImpl): MailScanEngine

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}

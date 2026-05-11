package org.jellyplex.client.di

import org.jellyplex.client.data.discovery.PlatformServerDiscovery
import org.jellyplex.client.data.local.SessionManager
import org.jellyplex.client.data.local.createSecureSettings
import org.jellyplex.client.data.remote.JellyfinApi
import org.jellyplex.client.data.repositories.AuthenticationRepository
import org.jellyplex.client.data.repositories.MediaRepository
import org.jellyplex.client.data.repositories.QuickConnectRepository
import org.jellyplex.client.domain.repositories.IAuthenticationRepository
import org.jellyplex.client.domain.repositories.IMediaRepository
import org.jellyplex.client.domain.repositories.IQuickConnectRepository
import org.jellyplex.client.domain.usecases.*
import org.jellyplex.client.ui.viewmodels.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {
    single { createSecureSettings() }
    singleOf(::SessionManager)
    single { JellyfinApi(sessionManager = get()) }
    single<IAuthenticationRepository> { AuthenticationRepository(get(), get()) }
    single<IMediaRepository> { MediaRepository(get(), get()) }
    single<IQuickConnectRepository> { QuickConnectRepository(get(), get()) }
    single<org.jellyplex.client.domain.discovery.IServerDiscovery> { PlatformServerDiscovery() }
}

val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::ResolveStreamConfigUseCase)
    factoryOf(::GetMoviesUseCase)
    factoryOf(::GetTvShowsUseCase)
    factoryOf(::GetHomeContentUseCase)
    factoryOf(::SearchItemsUseCase)
    factoryOf(::GetItemDetailsUseCase)
    factoryOf(::GetPeopleUseCase)
    factoryOf(::GetSeasonsUseCase)
    factoryOf(::GetEpisodesUseCase)
    factoryOf(::GetBaseUrlUseCase)
    factoryOf(::UpdateBaseUrlUseCase)
    factoryOf(::GetUserIdUseCase)
    factoryOf(::GetAccessTokenUseCase)
    factoryOf(::InitiateQuickConnectUseCase)
    factoryOf(::PollQuickConnectStatusUseCase)
    factoryOf(::ValidateServerUseCase)
    factoryOf(::HasSessionUseCase)
    factoryOf(::GetIsAuthenticatedUseCase)
    factoryOf(::ClearSessionUseCase)
    factoryOf(::ReportPlaybackStartUseCase)
    factoryOf(::ReportPlaybackProgressUseCase)
    factoryOf(::ReportPlaybackStoppedUseCase)
    factoryOf(::GetHomeCacheUseCase)
    factoryOf(::SaveHomeCacheUseCase)
    factoryOf(::GetMoviesCacheUseCase)
    factoryOf(::SaveMoviesCacheUseCase)
    factoryOf(::GetTvShowsCacheUseCase)
    factoryOf(::SaveTvShowsCacheUseCase)
    factoryOf(::DiscoverServersUseCase)
    factoryOf(::ValidateSessionUseCase)
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MoviesViewModel)
    viewModelOf(::TvShowsViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::QuickConnectViewModel)
    viewModelOf(::DiscoveryViewModel)
    viewModelOf(::DownloadsViewModel)
    viewModelOf(::SeriesDetailViewModel)
    viewModelOf(::MovieDetailViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::SessionViewModel)
}

fun appModule(): List<Module> = listOf(dataModule, domainModule, viewModelModule)
